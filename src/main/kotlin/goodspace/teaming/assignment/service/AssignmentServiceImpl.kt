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
import goodspace.teaming.global.entity.room.RoomRole
import goodspace.teaming.global.entity.room.UserRoom
import goodspace.teaming.global.entity.user.User
import goodspace.teaming.global.repository.FileRepository
import goodspace.teaming.global.repository.UserRepository
import goodspace.teaming.global.repository.UserRoomRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

private const val USER_ROOM_NOT_FOUND = "해당 티밍룸에 소속되어있지 않습니다."
private const val USER_NOT_FOUND = "해당 회원을 조회할 수 없습니다."
private const val INACCESSIBLE = "해당 티밍룸에 접근할 수 없습니다."
private const val MEMBER_FOR_ASSIGNED_NOT_FOUND = "과제를 할당할 회원을 조회할 수 없습니다."
private const val FILE_NOT_FOUND = "파일을 찾을 수 없습니다."
private const val NOT_LEADER = "팀장이 아닙니다."
private const val NOT_ASSIGNED = "해당 과제에 할당되지 않았습니다."
private const val ASSIGNMENT_NOT_FOUND = "과제를 찾을 수 없습니다."
private const val CANCELED_ASSIGNMENT = "취소된 과제입니다."
private const val COMPLETE_ASSIGNMENT = "이미 완료된 과제입니다."

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
        assertLeader(userRoom)

        val assignment = assignmentMapper.map(room, requestDto)
        val assignedMembers = requestDto.assignedMemberIds
            .toUserSet()
            .map { assignedMemberMapper.map(it, room, assignment) }

        assignment.addAssignedMembers(assignedMembers)

        room.addAssignment(assignment)
    }

    @Transactional(readOnly = true)
    override fun getAssignedAssignments(userId: Long): List<AssignmentPreviewResponseDto> {
        val user = findUser(userId)
        val referenceTime = Instant.now()

        return user.assignments
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
            ?: throw IllegalArgumentException(USER_ROOM_NOT_FOUND)
    }

    private fun findUser(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException(USER_NOT_FOUND) }
    }

    private fun assertPayment(userRoom: UserRoom) {
        check(userRoom.paymentStatus != NOT_PAID) { INACCESSIBLE }
    }

    private fun assertLeader(userRoom: UserRoom) {
        check(userRoom.roomRole == RoomRole.LEADER) { NOT_LEADER }
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

    private fun List<Long>.toUserSet(): Set<User> {
        return this.map {
            userRepository.findById(it)
                .orElseThrow { IllegalArgumentException(MEMBER_FOR_ASSIGNED_NOT_FOUND) }
        }.toSet()
    }

    private fun List<Assignment>.findById(id: Long): Assignment {
        return this.firstOrNull { it.id == id }
            ?: throw IllegalArgumentException(ASSIGNMENT_NOT_FOUND)
    }

    private fun List<File>.toSubmittedFiles(submission: Submission): List<SubmittedFile> {
        return this.map { SubmittedFile(submission, it) }
    }
}
