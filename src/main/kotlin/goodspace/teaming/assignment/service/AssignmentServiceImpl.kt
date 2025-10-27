package goodspace.teaming.assignment.service

import goodspace.teaming.assignment.domain.mapper.AssignedMemberMapper
import goodspace.teaming.assignment.domain.mapper.AssignmentMapper
import goodspace.teaming.assignment.domain.mapper.AssignmentPreviewMapper
import goodspace.teaming.assignment.dto.AssignmentCreateRequestDto
import goodspace.teaming.assignment.dto.AssignmentPreviewResponseDto
import goodspace.teaming.assignment.dto.AssignmentResponseDto
import goodspace.teaming.assignment.dto.SubmissionRequestDto
import goodspace.teaming.global.entity.aissgnment.Assignment
import goodspace.teaming.global.entity.aissgnment.AssignmentStatus.CANCELED
import goodspace.teaming.global.entity.aissgnment.AssignmentStatus.COMPLETE
import goodspace.teaming.global.entity.aissgnment.Submission
import goodspace.teaming.global.entity.file.SubmittedFile
import goodspace.teaming.global.entity.file.File
import goodspace.teaming.global.entity.room.PaymentStatus.NOT_PAID
import goodspace.teaming.global.entity.room.Room
import goodspace.teaming.global.entity.room.RoomRole
import goodspace.teaming.global.entity.room.UserRoom
import goodspace.teaming.global.entity.user.User
import goodspace.teaming.global.exception.*
import goodspace.teaming.global.repository.FileRepository
import goodspace.teaming.global.repository.UserRepository
import goodspace.teaming.global.repository.UserRoomRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class AssignmentServiceImpl(
    private val userRoomRepository: UserRoomRepository,
    private val userRepository: UserRepository,
    private val assignmentMapper: AssignmentMapper,
    private val assignedMemberMapper: AssignedMemberMapper,
    private val assignmentPreviewMapper: AssignmentPreviewMapper,
    private val fileRepository: FileRepository
) : AssignmentService {
    @Transactional
    override fun create(
        userId: Long,
        roomId: Long,
        requestDto: AssignmentCreateRequestDto
    ) {
        val userRoom = findUserRoom(userId, roomId)
        val room = userRoom.room

        assertPayment(userRoom)
        assertEveryMemberEntered(userRoom.room)
        assertLeader(userRoom)

        val assignment = assignmentMapper.map(room, requestDto)
        val assignedMembers = requestDto.assignedMemberIds
            .toUserRoomSet(room)
            .map { assignedMemberMapper.map(it, assignment) }

        assignment.addAssignedMembers(assignedMembers)

        room.addAssignment(assignment)
    }

    @Transactional(readOnly = true)
    override fun getAssignedAssignments(userId: Long): List<AssignmentPreviewResponseDto> {
        val user = findUser(userId)
        val referenceTime = Instant.now()

        return user.userRooms
            .flatMap { it.assignedMembers }
            .map { it.assignment }
            .filter { it.due.isAfter(referenceTime) }
            .map { assignmentPreviewMapper.map(it) }
    }

    @Transactional(readOnly = true)
    override fun getAssignmentsInRoom(
        userId: Long,
        roomId: Long
    ): List<AssignmentResponseDto> {
        val userRoom = findUserRoom(userId, roomId)
        val room = userRoom.room

        assertPayment(userRoom)

        return room.assignments
            .map { assignmentMapper.map(it) }
    }

    @Transactional
    override fun submit(
        userId: Long,
        roomId: Long,
        requestDto: SubmissionRequestDto
    ) {
        val userRoom = findUserRoom(userId, roomId)
        val submitter = userRoom.user
        val room = userRoom.room
        val assignment = room.assignments.findById(requestDto.assignmentId)

        assertPayment(userRoom)
        assertSubmitter(assignment, submitter)
        assertNotCanceled(assignment)

        val files = fileRepository.findAllById(requestDto.fileIds)
        require(files.size == requestDto.fileIds.size) { FILE_NOT_FOUND }

        val submission = Submission(
            assignment = assignment,
            submitterId = userId,
            description = requestDto.description
        )
        val submittedFiles = files.toSubmittedFiles(submission)
        submission.addSubmittedFiles(submittedFiles)

        assignment.addSubmission(submission)
        assignment.status = COMPLETE
    }

    @Transactional
    override fun cancel(
        userId: Long,
        roomId: Long,
        assignmentId: Long
    ) {
        val userRoom = findUserRoom(userId, roomId)
        val room = userRoom.room
        val assignment = room.assignments.findById(assignmentId)

        assertPayment(userRoom)
        assertLeader(userRoom)
        assertNotCompleted(assignment)

        assignment.status = CANCELED
    }

    private fun findUserRoom(userId: Long, roomId: Long): UserRoom {
        return userRoomRepository.findByRoomIdAndUserId(roomId, userId)
            ?: throw IllegalArgumentException(NOT_MEMBER_OF_ROOM)
    }

    private fun findUser(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException(USER_NOT_FOUND) }
    }

    private fun assertPayment(userRoom: UserRoom) {
        check(userRoom.paymentStatus != NOT_PAID) { ROOM_INACCESSIBLE }
    }

    private fun assertLeader(userRoom: UserRoom) {
        check(userRoom.roomRole == RoomRole.LEADER) { NOT_LEADER }
    }

    private fun assertEveryMemberEntered(room: Room) {
        check(room.everyMemberEnteredOrSuccess()) { EVERY_MEMBER_NOT_ENTERED }
    }

    private fun assertSubmitter(assignment: Assignment, submitter: User) {
        check(assignment.assignedMemberIds.contains(submitter.id)) { NOT_ASSIGNED }
    }

    private fun assertNotCanceled(assignment: Assignment) {
        check(assignment.status != CANCELED) { CANCELED_ASSIGNMENT }
    }

    private fun assertNotCompleted(assignment: Assignment) {
        check(assignment.status != COMPLETE) { COMPLETE_ASSIGNMENT }
    }

    private fun List<Long>.toUserRoomSet(room: Room): Set<UserRoom> {
        return this.map { userRepository.findById(it)
                .orElseThrow { IllegalArgumentException(MEMBER_TO_ASSIGN_NOT_FOUND) }
            }
            .flatMap { it.userRooms }
            .filter { it.room == room }
            .toSet()
    }

    private fun List<Assignment>.findById(id: Long): Assignment {
        return this.firstOrNull { it.id == id }
            ?: throw IllegalArgumentException(ASSIGNMENT_NOT_FOUND)
    }

    private fun List<File>.toSubmittedFiles(submission: Submission): List<SubmittedFile> {
        return this.map { SubmittedFile(submission, it) }
    }
}
