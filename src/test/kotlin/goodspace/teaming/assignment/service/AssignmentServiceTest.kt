package goodspace.teaming.assignment.service

import goodspace.teaming.assignment.domain.mapper.AssignedMemberMapper
import goodspace.teaming.assignment.domain.mapper.AssignmentMapper
import goodspace.teaming.assignment.domain.mapper.AssignmentPreviewMapper
import goodspace.teaming.assignment.dto.AssignmentCreateRequestDto
import goodspace.teaming.assignment.dto.AssignmentPreviewResponseDto
import goodspace.teaming.assignment.dto.AssignmentResponseDto
import goodspace.teaming.assignment.dto.SubmissionRequestDto
import goodspace.teaming.fixture.*
import goodspace.teaming.global.entity.aissgnment.AssignedMember
import goodspace.teaming.global.entity.aissgnment.Assignment
import goodspace.teaming.global.entity.aissgnment.AssignmentStatus
import goodspace.teaming.global.entity.room.*
import goodspace.teaming.global.entity.user.TeamingUser
import goodspace.teaming.global.entity.user.User
import goodspace.teaming.global.exception.CANCELED_ASSIGNMENT
import goodspace.teaming.global.repository.FileRepository
import goodspace.teaming.global.repository.UserRepository
import goodspace.teaming.global.repository.UserRoomRepository
import goodspace.teaming.util.createAssignedMember
import goodspace.teaming.util.createRoom
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils
import java.time.Instant
import java.util.*
import kotlin.IllegalStateException

private const val ASSIGNED_MEMBER_ID = 999L

class AssignmentServiceTest {
    private val userRoomRepository = mockk<UserRoomRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val assignmentMapper = mockk<AssignmentMapper>(relaxed = true)
    private val assignedMemberMapper = mockk<AssignedMemberMapper>(relaxed = true)
    private val assignmentPreviewMapper = mockk<AssignmentPreviewMapper>()
    private val fileRepository = mockk<FileRepository>(relaxed = true)

    private val assignmentService = AssignmentServiceImpl(
        userRoomRepository = userRoomRepository,
        userRepository = userRepository,
        assignmentMapper = assignmentMapper,
        assignedMemberMapper = assignedMemberMapper,
        assignmentPreviewMapper = assignmentPreviewMapper,
        fileRepository = fileRepository
    )

    @Nested
    @DisplayName("create")
    inner class Create {
        @Test
        fun `새로운 과제를 생성하여 티밍룸에 추가한다`() {
            // given
            val leader = createUser()
            val assignedMember = createUser(id = ASSIGNED_MEMBER_ID)
            val room = createRoom()

            val leaderUserRoom = createUserRoom(
                user = leader,
                room = room,
                roomRole = RoomRole.LEADER,
                paymentStatus = PaymentStatus.PAID
            )
            val requestDto = AssignmentCreateRequestDto(
                title = ASSIGNMENT_TITLE,
                description = ASSIGNMENT_DESCRIPTION,
                assignedMemberIds = listOf(assignedMember.id!!),
                due = ASSIGNMENT_DUE
            )
            val expectedAssignment = Assignment(
                title = ASSIGNMENT_TITLE,
                description = ASSIGNMENT_DESCRIPTION,
                room = room,
                due = ASSIGNMENT_DUE
            )

            every { userRoomRepository.findByRoomIdAndUserId(room.id!!, leader.id!!) } returns leaderUserRoom
            every { assignmentMapper.map(room, requestDto) } returns expectedAssignment
            every { userRepository.findById(assignedMember.id!!) } returns Optional.of(assignedMember)

            // when
            assignmentService.create(leader.id!!, room.id!!, requestDto)

            // then
            assertThat(room.assignments).containsExactly(expectedAssignment)
        }

        @Test
        fun `결제 되지 않았다면 예외를 던진다`() {
            // given
            val leader = createUser()
            val room = createRoom()

            val notPaidUserRoom = createUserRoom(
                user = leader,
                room = room,
                roomRole = RoomRole.LEADER,
                paymentStatus = PaymentStatus.NOT_PAID
            )
            val requestDto = createAssignmentCreateRequestDto()

            every { userRoomRepository.findByRoomIdAndUserId(room.id!!, leader.id!!) } returns notPaidUserRoom

            // when & then
            assertThatThrownBy { assignmentService.create(leader.id!!, room.id!!, requestDto) }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("해당 티밍룸에 접근할 수 없습니다.")
        }

        @Test
        fun `팀장이 아니라면 예외를 던진다`() {
            // given
            val member = createUser()
            val room = createRoom()

            val notLeaderUserRoom = createUserRoom(
                user = member,
                room = room,
                roomRole = RoomRole.MEMBER,
                paymentStatus = PaymentStatus.PAID
            )
            val requestDto = createAssignmentCreateRequestDto()

            every { userRoomRepository.findByRoomIdAndUserId(room.id!!, member.id!!) } returns notLeaderUserRoom

            // when & then
            assertThatThrownBy { assignmentService.create(member.id!!, room.id!!, requestDto) }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("팀장이 아닙니다.")
        }
    }

    @Nested
    @DisplayName("getAssignedAssignments")
    inner class GetAssignedAssignments {
        @Test
        fun `해당 회원에게 할당되었으면서 제출 기한이 지나지 않은 과제들을 반환한다`() {
            // given
            val user = mockk<User>()
            val userRoom = mockk<UserRoom>()
            val notClosedAssignedMember = mockk<AssignedMember>()
            val closedAssignedMember = mockk<AssignedMember>()
            every { userRepository.findById(USER_ID) } returns Optional.of(user)

            val notClosedAssignment = mockk<Assignment>()
            val closedAssignment = mockk<Assignment>()

            every { user.userRooms } returns mutableListOf(userRoom)
            every { userRoom.assignedMembers } returns mutableListOf(notClosedAssignedMember)
            every { notClosedAssignedMember.assignment } returns notClosedAssignment
            every { closedAssignedMember.assignment } returns closedAssignment
            every { notClosedAssignment.due.isAfter(any()) } returns true
            every { closedAssignment.due.isAfter(any()) } returns false

            val expectedDto = mockk<AssignmentPreviewResponseDto>()
            val notExpectedDto = mockk<AssignmentPreviewResponseDto>()

            every { assignmentPreviewMapper.map(notClosedAssignment) } returns expectedDto
            every { assignmentPreviewMapper.map(closedAssignment) } returns notExpectedDto

            // when
            val response = assignmentService.getAssignedAssignments(USER_ID)

            // then
            assertThat(response).containsExactly(expectedDto)
        }
    }

    @Nested
    @DisplayName("getAssignmentsInRoom")
    inner class GetAssignmentsInRoom {
        @Test
        fun `해당 티밍룸의 모든 과제를 반환한다`() {
            // given
            val user = createUser()
            val room = createRoom()

            val userRoom = createUserRoom(
                user = user,
                room = room,
                paymentStatus = PaymentStatus.PAID
            )

            val assignment1 = createAssignment(
                room = room,
                id = 1
            )
            val assignment2 = createAssignment(
                room = room,
                id = 2
            )
            room.addAssignments(assignment1, assignment2)

            every { userRoomRepository.findByRoomIdAndUserId(room.id!!, user.id!!) } returns userRoom
            val assignmentDto1 = assignment1.toDto()
            val assignmentDto2 = assignment2.toDto()
            every { assignmentMapper.map(assignment1) } returns assignmentDto1
            every { assignmentMapper.map(assignment2) } returns assignmentDto2

            // when
            val result = assignmentService.getAssignmentsInRoom(user.id!!, room.id!!)

            // then
            assertThat(result).containsExactlyInAnyOrder(assignmentDto1, assignmentDto2)
        }

        @Test
        fun `결제 되지 않았다면 예외를 던진다`() {
            // given
            val user = createUser()
            val room = createRoom()

            val notPaidUserRoom = createUserRoom(
                user = user,
                room = room,
                paymentStatus = PaymentStatus.NOT_PAID
            )

            every { userRoomRepository.findByRoomIdAndUserId(room.id!!, user.id!!) } returns notPaidUserRoom

            // when & then
            assertThatThrownBy { assignmentService.getAssignmentsInRoom(user.id!!, room.id!!) }
                .isInstanceOf(IllegalStateException::class.java)
        }
    }

    @Nested
    @DisplayName("submit")
    inner class Submit {
        @Test
        fun `과제를 제출한다(Submission을 생성해 연결한다)`() {
            // given: 과제를 할당받은 팀원이라고 가정함
            val submitter = createUser()
            val room = createRoom()
            val userRoom = createUserRoom(
                user = submitter,
                room = room,
                paymentStatus = PaymentStatus.PAID
            )
            val assignment = createAssignment(
                room = room,
            )
            val assigned = createAssignedMember(
                userRoom = userRoom,
                assignment = assignment,
            )
            room.addAssignment(assignment)
            assignment.addAssignedMember(assigned)

            val requestDto = SubmissionRequestDto(
                assignmentId = assignment.id!!,
                description = assignment.description,
                fileIds = listOf()
            )

            every { userRoomRepository.findByRoomIdAndUserId(room.id!!, submitter.id!!) } returns userRoom

            // when
            assignmentService.submit(submitter.id!!, room.id!!, requestDto)

            // then
            assertThat(assignment.submissions).hasSize(1)

            val submission = assignment.submissions[0]
            assertThat(submission.assignment).isEqualTo(assignment)
            assertThat(submission.submitterId).isEqualTo(submitter.id)
            assertThat(submission.description).isEqualTo(assignment.description)
        }

        @Test
        fun `과제의 상태를 '완료됨'으로 변경한다`() {
            // given: 과제를 할당받은 팀원이라고 가정함
            val submitter = createUser()
            val room = createRoom()
            val userRoom = createUserRoom(
                user = submitter,
                room = room,
                paymentStatus = PaymentStatus.PAID
            )
            val assignment = createAssignment(
                room = room,
                status = AssignmentStatus.IN_PROGRESS
            )
            val assigned = createAssignedMember(
                userRoom = userRoom,
                assignment = assignment,
            )
            room.addAssignment(assignment)
            assignment.addAssignedMember(assigned)

            val requestDto = SubmissionRequestDto(
                assignmentId = assignment.id!!,
                description = assignment.description,
                fileIds = listOf()
            )

            every { userRoomRepository.findByRoomIdAndUserId(room.id!!, submitter.id!!) } returns userRoom

            // when
            assignmentService.submit(submitter.id!!, room.id!!, requestDto)

            // then
            assertThat(assignment.status).isEqualTo(AssignmentStatus.COMPLETE)
        }

        @Test
        fun `취소된 과제라면 예외가 발생한다`() {
            // given
            val member = createUser()
            val room = createRoom(id = ROOM_ID)
            val userRoom = createUserRoom(
                user = member,
                room = room,
                paymentStatus = PaymentStatus.PAID
            )
            val assignment = createAssignment(
                room = room,
                id = ASSIGNMENT_ID,
                status = AssignmentStatus.CANCELED
            )
            val assigned = createAssignedMember(
                userRoom = userRoom,
                assignment = assignment,
                id = ASSIGNED_MEMBER_ID
            )
            room.addAssignment(assignment)
            assignment.addAssignedMember(assigned)

            val requestDto = SubmissionRequestDto(
                assignmentId = assignment.id!!,
                description = assignment.description,
                fileIds = listOf()
            )

            every { userRoomRepository.findByRoomIdAndUserId(room.id!!, member.id!!) } returns userRoom

            // when & then
            assertThatThrownBy { assignmentService.submit(member.id!!, room.id!!, requestDto) }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage(CANCELED_ASSIGNMENT)
        }

        @Test
        fun `해당 과제에 할당된 팀원이 아니라면 예외가 발생한다`() {
            // given: 해당 과제를 할당받지 않은 팀원이라고 가정함
            val notAssignedMember = createUser()
            val room = createRoom()
            val userRoom = createUserRoom(
                user = notAssignedMember,
                room = room,
                paymentStatus = PaymentStatus.PAID
            )
            val assignment = createAssignment(
                room = room,
                status = AssignmentStatus.IN_PROGRESS
            )
            room.addAssignment(assignment)

            val requestDto = SubmissionRequestDto(
                assignmentId = assignment.id!!,
                description = assignment.description,
                fileIds = listOf()
            )

            every { userRoomRepository.findByRoomIdAndUserId(room.id!!, notAssignedMember.id!!) } returns userRoom

            // when & then
            assertThatThrownBy { assignmentService.submit(notAssignedMember.id!!, room.id!!, requestDto) }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("해당 과제에 할당되지 않았습니다.")
        }

        @Test
        fun `결제 되지 않았다면 예외가 발생한다`() {
            // given
            val member = createUser()
            val room = createRoom()
            val notPaidUserRoom = createUserRoom(
                user = member,
                room = room,
                paymentStatus = PaymentStatus.NOT_PAID
            )
            val assignment = createAssignment(
                room = room,
                status = AssignmentStatus.IN_PROGRESS
            )
            val assigned = createAssignedMember(
                userRoom = notPaidUserRoom,
                assignment = assignment,
            )
            assignment.addAssignedMember(assigned)
            room.addAssignment(assignment)

            val requestDto = SubmissionRequestDto(
                assignmentId = assignment.id!!,
                description = assignment.description,
                fileIds = listOf()
            )

            every { userRoomRepository.findByRoomIdAndUserId(room.id!!, member.id!!) } returns notPaidUserRoom

            // when & then
            assertThatThrownBy { assignmentService.submit(member.id!!, room.id!!, requestDto) }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("해당 티밍룸에 접근할 수 없습니다.")
        }
    }

    @Nested
    @DisplayName("cancel")
    inner class Cancel {
        @Test
        fun `과제의 상태를 '취소됨'으로 설정한다`() {
            // given
            val leader = createUser()
            val room = createRoom()
            val userRoom = createUserRoom(
                user = leader,
                room = room,
                roomRole = RoomRole.LEADER,
                paymentStatus = PaymentStatus.PAID
            )
            val assignment = createAssignment(
                room = room,
                status = AssignmentStatus.IN_PROGRESS
            )
            room.addAssignment(assignment)

            every { userRoomRepository.findByRoomIdAndUserId(room.id!!, leader.id!!) } returns userRoom

            // when
            assignmentService.cancel(leader.id!!, room.id!!, assignment.id!!)

            // then
            assertThat(assignment.status).isEqualTo(AssignmentStatus.CANCELED)
        }

        @Test
        fun `이미 완료된 과제라면 예외가 발생한다`() {
            // given
            val leader = createUser()
            val room = createRoom()
            val userRoom = createUserRoom(
                user = leader,
                room = room,
                roomRole = RoomRole.LEADER,
                paymentStatus = PaymentStatus.PAID
            )
            val completedAssignment = createAssignment(
                room = room,
                status = AssignmentStatus.COMPLETE
            )
            room.addAssignment(completedAssignment)

            every { userRoomRepository.findByRoomIdAndUserId(room.id!!, leader.id!!) } returns userRoom

            // when & then
            assertThatThrownBy { assignmentService.cancel(leader.id!!, room.id!!, completedAssignment.id!!) }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("이미 완료된 과제입니다.")
        }

        @Test
        fun `팀장이 아니라면 예외가 발생한다`() {
            // given
            val member = createUser()
            val room = createRoom()
            val memberUserRoom = createUserRoom(
                user = member,
                room = room,
                roomRole = RoomRole.MEMBER,
                paymentStatus = PaymentStatus.PAID
            )
            val assignment = createAssignment(
                room = room,
                status = AssignmentStatus.IN_PROGRESS
            )
            room.addAssignment(assignment)

            every { userRoomRepository.findByRoomIdAndUserId(room.id!!, member.id!!) } returns memberUserRoom

            // when & then
            assertThatThrownBy { assignmentService.cancel(member.id!!, room.id!!, assignment.id!!) }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("팀장이 아닙니다.")
        }

        @Test
        fun `결제 되지 않았다면 예외가 발생한다`() {
            // given
            val leader = createUser()
            val room = createRoom()
            val notPaidLeaderUserRoom = createUserRoom(
                user = leader,
                room = room,
                roomRole = RoomRole.LEADER,
                paymentStatus = PaymentStatus.NOT_PAID
            )
            val assignment = createAssignment(
                room = room,
                status = AssignmentStatus.IN_PROGRESS
            )
            room.addAssignment(assignment)

            every { userRoomRepository.findByRoomIdAndUserId(room.id!!, leader.id!!) } returns notPaidLeaderUserRoom

            // when & then
            assertThatThrownBy { assignmentService.cancel(leader.id!!, room.id!!, assignment.id!!) }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("해당 티밍룸에 접근할 수 없습니다.")
        }
    }

    private fun createUser(
        email: String = USER_EMAIL,
        password: String = USER_PASSWORD,
        name: String = USER_NAME,
        id: Long = USER_ID
    ): User {
        val user = TeamingUser(
            email = email,
            password = password,
            name = name,
        )
        ReflectionTestUtils.setField(user, "id", id)

        return user
    }

    private fun createUserRoom(
        user: User,
        room: Room,
        roomRole: RoomRole = USER_ROOM_ROLE,
        lastReadMessageId: Long? = USER_ROOM_LAST_READ_MESSAGE_ID,
        paymentStatus: PaymentStatus = USER_ROOM_PAYMENT_STATUS,
        id: Long = USER_ROOM_ID
    ): UserRoom {
        val userRoom = UserRoom(
            user = user,
            room = room,
            roomRole = roomRole,
            lastReadMessageId = lastReadMessageId,
            paymentStatus = paymentStatus
        )
        ReflectionTestUtils.setField(userRoom, "id", id)

        return userRoom
    }

    private fun createAssignment(
        room: Room,
        title: String = ASSIGNMENT_TITLE,
        description: String = ASSIGNMENT_DESCRIPTION,
        due: Instant = ASSIGNMENT_DUE,
        status: AssignmentStatus = ASSIGNMENT_STATUS,
        id: Long = ASSIGNMENT_ID
    ): Assignment {
        val assignment = Assignment(
            room = room,
            title = title,
            description = description,
            due = due,
            status = status
        )
        ReflectionTestUtils.setField(assignment, "id", id)

        return assignment
    }

    private fun createAssignmentCreateRequestDto(
        title: String = ASSIGNMENT_TITLE,
        description: String = ASSIGNMENT_DESCRIPTION,
        assignedMemberIds: List<Long> = listOf(),
        due: Instant = ASSIGNMENT_DUE
    ): AssignmentCreateRequestDto {
        return AssignmentCreateRequestDto(
            title = title,
            description = description,
            assignedMemberIds = assignedMemberIds,
            due = due
        )
    }

    private fun Assignment.toDto(): AssignmentResponseDto {
        return AssignmentResponseDto(
            assignmentId = id!!,
            title = title,
            description = description,
            assignedMemberIds = assignedMemberIds,
            due = due,
            status = status,
            submissions = listOf(),
            punished = punished
        )
    }
}
