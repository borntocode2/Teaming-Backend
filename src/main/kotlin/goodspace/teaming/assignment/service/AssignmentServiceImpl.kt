package goodspace.teaming.assignment.service

import goodspace.teaming.assignment.domain.mapper.AssignedMemberMapper
import goodspace.teaming.assignment.domain.mapper.AssignmentMapper
import goodspace.teaming.assignment.dto.AssignmentCreateRequestDto
import goodspace.teaming.assignment.dto.AssignmentResponseDto
import goodspace.teaming.assignment.dto.SubmissionRequestDto
import goodspace.teaming.global.entity.aissgnment.Assignment
import goodspace.teaming.global.entity.aissgnment.AssignmentStatus.COMPLETE
import goodspace.teaming.global.entity.aissgnment.Submission
import goodspace.teaming.global.entity.aissgnment.SubmittedFile
import goodspace.teaming.global.entity.file.File
import goodspace.teaming.global.entity.room.PaymentStatus.NOT_PAID
import goodspace.teaming.global.entity.room.UserRoom
import goodspace.teaming.global.entity.user.User
import goodspace.teaming.global.repository.FileRepository
import goodspace.teaming.global.repository.UserRepository
import goodspace.teaming.global.repository.UserRoomRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private const val USER_ROOM_NOT_FOUND = "해당 티밍룸에 소속되어있지 않습니다."
private const val INACCESSIBLE = "해당 티밍룸에 접근할 수 없습니다."
private const val MEMBER_FOR_ASSIGNED_NOT_FOUND = "과제를 할당할 회원을 조회할 수 없습니다."
private const val FILE_NOT_FOUND = "파일을 찾을 수 없습니다."

@Service
class AssignmentServiceImpl(
    private val userRoomRepository: UserRoomRepository,
    private val userRepository: UserRepository,
    private val assignmentMapper: AssignmentMapper,
    private val assignedMemberMapper: AssignedMemberMapper,
    private val fileRepository: FileRepository
) : AssignmentService {
    @Transactional
    override fun create(userId: Long, roomId: Long, requestDto: AssignmentCreateRequestDto) {
        val userRoom = findUserRoom(userId, roomId)
        val room = userRoom.room

        assertPayment(userRoom)

        val assignment = assignmentMapper.map(room, requestDto)
        val assignedMembers = requestDto.assignedMemberIds
            .toUserSet()
            .map { assignedMemberMapper.map(it, room, assignment) }

        assignment.addAssignedMembers(assignedMembers)
    }

    @Transactional(readOnly = true)
    override fun get(userId: Long, roomId: Long): List<AssignmentResponseDto> {
        val userRoom = findUserRoom(userId, roomId)
        val room = userRoom.room

        assertPayment(userRoom)

        return room.assignments
            .map { assignmentMapper.map(it) }
    }

    @Transactional
    override fun submit(userId: Long, roomId: Long, requestDto: SubmissionRequestDto) {
        val userRoom = findUserRoom(userId, roomId)
        val room = userRoom.room

        assertPayment(userRoom)

        val files = fileRepository.findAllById(requestDto.fileIds)
        require(files.size == requestDto.fileIds.size) { FILE_NOT_FOUND }

        val assignment = room.assignments.findById(requestDto.assignmentId)
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

    private fun findUserRoom(userId: Long, roomId: Long): UserRoom {
        return userRoomRepository.findByRoomIdAndUserId(roomId, userId)
            ?: throw IllegalArgumentException(USER_ROOM_NOT_FOUND)
    }

    private fun assertPayment(userRoom: UserRoom) {
        require(userRoom.paymentStatus != NOT_PAID) { INACCESSIBLE }
    }

    private fun List<Long>.toUserSet(): Set<User> {
        return this.map {
            userRepository.findById(it)
                .orElseThrow { IllegalArgumentException(MEMBER_FOR_ASSIGNED_NOT_FOUND) }
        }.toSet()
    }

    private fun List<Assignment>.findById(id: Long): Assignment {
        return this.firstOrNull { it.id == id }
            ?: throw IllegalArgumentException(FILE_NOT_FOUND)
    }

    private fun List<File>.toSubmittedFiles(submission: Submission): List<SubmittedFile> {
        return this.map { SubmittedFile(submission, it) }
    }
}
