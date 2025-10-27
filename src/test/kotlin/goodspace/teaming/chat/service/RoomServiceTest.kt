package goodspace.teaming.chat.service

import goodspace.teaming.chat.domain.InviteCodeGenerator
import goodspace.teaming.chat.domain.mapper.*
import goodspace.teaming.chat.dto.InviteAcceptRequestDto
import goodspace.teaming.chat.dto.RoomCreateRequestDto
import goodspace.teaming.chat.dto.RoomInfoResponseDto
import goodspace.teaming.chat.dto.RoomSearchResponseDto
import goodspace.teaming.chat.event.MemberEnteredEvent
import goodspace.teaming.chat.event.RoomLeaveEvent
import goodspace.teaming.chat.exception.InviteCodeAllocationFailedException
import goodspace.teaming.global.entity.room.*
import goodspace.teaming.global.entity.user.User
import goodspace.teaming.global.exception.EVERY_MEMBER_NOT_ENTERED
import goodspace.teaming.global.exception.NOT_LEADER
import goodspace.teaming.global.repository.RoomRepository
import goodspace.teaming.global.repository.UserRepository
import goodspace.teaming.global.repository.UserRoomRepository
import goodspace.teaming.util.createRoom
import goodspace.teaming.util.createUser
import goodspace.teaming.util.createUserRoom
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import java.util.*

private const val TITLE = "티밍룸 제목"
private const val DESCRIPTION = "티밍룸 설명"
private val ROOM_TYPE = RoomType.BASIC
private const val MEMBER_COUNT = 5
private const val USER_ID = 1L
private const val ROOM_ID = 10L
private const val INVITE_CODE = "ABC123"
private const val DUPLICATE_CODE = "DUPLICATE"
private const val UNIQUE_CODE = "UNIQUE"

class RoomServiceTest {
    private val userRepository: UserRepository = mockk()
    private val roomRepository: RoomRepository = mockk(relaxed = true)
    private val userRoomRepository: UserRoomRepository = mockk()
    private val roomMapper: RoomMapper = mockk()
    private val roomInfoMapper: RoomInfoMapper = mockk(relaxed = true)
    private val memberMapper: RoomMemberMapper = mockk(relaxed = true)
    private val roomSearchMapper: RoomSearchMapper = mockk(relaxed = true)
    private val roomReadyMapper: RoomReadyMapper = mockk(relaxed = true)
    private val inviteCodeGenerator: InviteCodeGenerator = mockk()
    private val eventPublisher: ApplicationEventPublisher = mockk(relaxed = true)

    private val roomService = RoomServiceImpl(
        userRepository = userRepository,
        roomRepository = roomRepository,
        userRoomRepository = userRoomRepository,
        roomMapper = roomMapper,
        roomInfoMapper = roomInfoMapper,
        memberMapper = memberMapper,
        roomSearchMapper = roomSearchMapper,
        roomReadyMapper = roomReadyMapper,
        inviteCodeGenerator = inviteCodeGenerator,
        eventPublisher = eventPublisher
    )

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @Nested
    @DisplayName("createRoom")
    inner class CreateRoom {
        @Test
        fun `팀장이 되어 새로운 티밍룸을 생성한다`() {
            // given
            val leader = createUser(id = USER_ID)
            every { userRepository.findById(USER_ID) } returns Optional.of(leader)

            val room = createRoom()
            every { roomMapper.map(any()) } returns room

            every { inviteCodeGenerator.generate() } returns UNIQUE_CODE
            every { roomRepository.existsByInviteCode(UNIQUE_CODE) } returns false
            every { roomRepository.saveAndFlush(room) } returns room

            // when
            roomService.createRoom(USER_ID, getRoomCreateDto())

            // then
            assertThat(room.inviteCode).isEqualTo(UNIQUE_CODE)
            assertThat(room.userRooms).hasSize(1)

            val leaderUserRoom = room.userRooms.first()
            assertThat(leaderUserRoom.roomRole).isEqualTo(RoomRole.LEADER)
            assertThat(leaderUserRoom.user).isSameAs(leader)
            assertThat(leaderUserRoom.room).isSameAs(room)
        }

        @Test
        fun `중복되지 않는 초대코드를 설정한다`() {
            // given
            val user = createUser(id = USER_ID)
            every { userRepository.findById(USER_ID) } returns Optional.of(user)

            val room = createRoom()
            every { roomMapper.map(any()) } returns room

            // 두 번까지는 중복된 초대코드를 발행하도록 설정
            every { inviteCodeGenerator.generate() } returnsMany listOf(DUPLICATE_CODE, DUPLICATE_CODE, UNIQUE_CODE)
            every { roomRepository.existsByInviteCode(DUPLICATE_CODE) } returns true
            every { roomRepository.existsByInviteCode(UNIQUE_CODE) } returns false
            every { roomRepository.saveAndFlush(room) } returns room

            // when
            roomService.createRoom(USER_ID, getRoomCreateDto())

            // then
            assertThat(room.inviteCode).isEqualTo(UNIQUE_CODE)
        }

        @Test
        fun `계속해서 중복되지 않은 초대코드 발행에 실패하면 예외를 던진다`() {
            // given
            val user = createUser(id = USER_ID)
            every { userRepository.findById(USER_ID) } returns Optional.of(user)

            val room = createRoom()
            every { roomMapper.map(any()) } returns room

            every { inviteCodeGenerator.generate() } returns DUPLICATE_CODE
            every { roomRepository.existsByInviteCode(DUPLICATE_CODE) } returns true

            // when & then
            assertThatThrownBy { roomService.createRoom(USER_ID, getRoomCreateDto()) }
                .isInstanceOf(InviteCodeAllocationFailedException::class.java)
        }

        @Test
        fun `초대 코드를 반환한다`() {
            // given
            val user = createUser(id = USER_ID)
            every { userRepository.findById(USER_ID) } returns Optional.of(user)

            val room = createRoom()
            every { roomMapper.map(any()) } returns room

            every { inviteCodeGenerator.generate() } returns UNIQUE_CODE
            every { roomRepository.existsByInviteCode(UNIQUE_CODE) } returns false
            every { roomRepository.saveAndFlush(room) } returns room

            // when
            val responseDto = roomService.createRoom(USER_ID, getRoomCreateDto())

            // then
            assertThat(responseDto.inviteCode).isEqualTo(UNIQUE_CODE)
        }

        @Test
        fun `티밍룸 타입이 Demo일 경우 결제 상태를 '결제됨'으로 설정한다`() {
            // given
            val user = createUser(id = USER_ID)
            every { userRepository.findById(USER_ID) } returns Optional.of(user)

            val room = createRoom(type = RoomType.DEMO)
            every { roomMapper.map(any()) } returns room

            every { inviteCodeGenerator.generate() } returns UNIQUE_CODE
            every { roomRepository.existsByInviteCode(UNIQUE_CODE) } returns false
            every { roomRepository.saveAndFlush(room) } returns room

            // when
            roomService.createRoom(USER_ID, getRoomCreateDto())

            // then
            assertThat(user.userRooms.size).isEqualTo(1)
            val userRoom = user.userRooms[0]
            assertThat(userRoom.paymentStatus).isEqualTo(PaymentStatus.PAID)
        }
    }

    @Nested
    @DisplayName("searchRoom")
    inner class SearchRoom {
        @Nested
        @DisplayName("searchRoom")
        inner class SearchRoom {
            @Test
            fun `초대코드로 방을 조회하고 DTO로 매핑하여 반환한다`() {
                // given
                val room = createRoom(inviteCode = INVITE_CODE)
                val dto = mockk<RoomSearchResponseDto>()

                every { roomRepository.findByInviteCode(INVITE_CODE) } returns room
                every { roomSearchMapper.map(room) } returns dto

                // when
                val result = roomService.searchRoom(INVITE_CODE)

                // then
                assertThat(result).isSameAs(dto)
            }

            @Test
            fun `존재하지 않는 초대코드면 예외를 던진다`() {
                // given
                every { roomRepository.findByInviteCode(INVITE_CODE) } returns null

                // when & then
                assertThatThrownBy { roomService.searchRoom(INVITE_CODE) }
                    .isInstanceOf(IllegalArgumentException::class.java)
            }
        }
    }

    @Nested
    @DisplayName("acceptInvite")
    inner class AcceptInvite {
        @Test
        fun `초대를 수락하여 방의 멤버로 합류한다`() {
            // given
            val user = mockk<User>(relaxed = true)
            val room = createRoom(inviteCode = INVITE_CODE)

            every { userRepository.findById(USER_ID) } returns Optional.of(user)
            every { roomRepository.findByInviteCode(INVITE_CODE) } returns room
            every { userRoomRepository.existsByRoomAndUser(room, user) } returns false

            // when
            roomService.acceptInvite(USER_ID, InviteAcceptRequestDto(inviteCode = INVITE_CODE))

            // then
            assertThat(room.userRooms).hasSize(1)

            val membership = room.userRooms.first()
            assertThat(membership.roomRole).isEqualTo(RoomRole.MEMBER)
            assertThat(membership.user).isSameAs(user)
            assertThat(membership.room).isSameAs(room)
        }

        @Test
        fun `멤버 입장 이벤트를 발행한다`() {
            // given
            val user = createUser()
            val room = createRoom()
            val requestDto = InviteAcceptRequestDto(room.inviteCode!!)

            every { userRepository.findById(user.id!!) } returns Optional.of(user)
            every { roomRepository.findByInviteCode(room.inviteCode!!) } returns room
            every { userRoomRepository.existsByRoomAndUser(room, user) } returns false

            // when
            roomService.acceptInvite(user.id!!, requestDto)

            // then
            verify(exactly = 1) { eventPublisher.publishEvent(ofType<MemberEnteredEvent>()) }
        }

        @Test
        fun `존재하지 않는 초대코드면 예외를 던진다`() {
            // given
            val user = mockk<User>(relaxed = true)
            every { userRepository.findById(USER_ID) } returns Optional.of(user)
            every { roomRepository.findByInviteCode(INVITE_CODE) } returns null

            // when & then
            assertThatThrownBy { roomService.acceptInvite(USER_ID, InviteAcceptRequestDto(inviteCode = INVITE_CODE)) }
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun `이미 멤버인 사용자가 초대를 수락하면 예외를 던진다`() {
            // given: 이미 멤버인 상황
            val user = mockk<User>(relaxed = true)
            val room = createRoom(inviteCode = INVITE_CODE)

            every { userRepository.findById(USER_ID) } returns Optional.of(user)
            every { roomRepository.findByInviteCode(INVITE_CODE) } returns room
            every { userRoomRepository.existsByRoomAndUser(room, user) } returns true
            every { roomInfoMapper.map(any()) } returns mockk<RoomInfoResponseDto>(relaxed = true)

            // when & then
            assertThatThrownBy { roomService.acceptInvite(USER_ID, InviteAcceptRequestDto(inviteCode = INVITE_CODE)) }
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun `초대 수락에 성공하면 해당 티밍룸에 대한 DTO를 반환한다`() {
            // given
            val user = mockk<User>(relaxed = true)
            val room = createRoom(INVITE_CODE)
            val roomDto = mockk<RoomInfoResponseDto>(relaxed = true)

            every { userRepository.findById(USER_ID) } returns Optional.of(user)
            every { roomRepository.findByInviteCode(INVITE_CODE) } returns room
            every { userRoomRepository.existsByRoomAndUser(room, user) } returns false
            every { roomInfoMapper.map(any()) } returns roomDto

            // when
            val result = roomService.acceptInvite(USER_ID, InviteAcceptRequestDto(inviteCode = INVITE_CODE))

            // then
            assertThat(result).isSameAs(roomDto)
        }

        @Test
        fun `티밍룸 타입이 Demo일 경우 결제 상태를 '결제됨'으로 설정한다`() {
            // given
            val user = createUser()
            val room = createRoom(type = RoomType.DEMO)
            val requestDto = InviteAcceptRequestDto(inviteCode = room.inviteCode!!)

            every { userRepository.findById(user.id!!) } returns Optional.of(user)
            every { roomRepository.findByInviteCode(room.inviteCode!!) } returns room
            every { userRoomRepository.existsByRoomAndUser(room, user) } returns false

            // when
            roomService.acceptInvite(user.id!!, requestDto)

            // then
            assertThat(user.userRooms.size).isEqualTo(1)
            val userRoom = user.userRooms[0]
            assertThat(userRoom.paymentStatus).isEqualTo(PaymentStatus.PAID)
        }
    }

    @Nested
    @DisplayName("getInviteCode")
    inner class GetInviteCode {
        @Test
        fun `방의 초대 코드를 반환한다`() {
            // given
            val user = mockk<User>(relaxed = true)
            val room = createRoom(inviteCode = INVITE_CODE)
            val userRoom = UserRoom(
                user = user,
                room = room,
                roomRole = RoomRole.LEADER
            )

            every { userRoomRepository.findByRoomIdAndUserId(ROOM_ID, USER_ID) } returns userRoom

            // when
            val responseDto = roomService.getInviteCode(USER_ID, ROOM_ID)

            // then
            assertThat(responseDto.inviteCode).isEqualTo(INVITE_CODE)
        }

        @Test
        fun `팀장이 아니라면 예외가 발생한다`() {
            // given
            val user = mockk<User>(relaxed = true)
            val room = createRoom()
            val userRoom = UserRoom(
                user = user,
                room = room,
                roomRole = RoomRole.MEMBER
            )

            every { userRoomRepository.findByRoomIdAndUserId(ROOM_ID, USER_ID) } returns userRoom

            // when & then
            assertThatThrownBy { roomService.getInviteCode(USER_ID, ROOM_ID) }
                .isInstanceOf(IllegalStateException::class.java)
        }
    }

    @Nested
    @DisplayName("getRooms")
    inner class GetRooms {
        @Test
        fun `회원의 UserRoom들을 매핑하여 반환한다`() {
            // given
            val user = mockk<User>()
            val ur1 = mockk<UserRoom>()
            val ur2 = mockk<UserRoom>()
            val dto1 = mockk<RoomInfoResponseDto>()
            val dto2 = mockk<RoomInfoResponseDto>()

            every { userRepository.findById(USER_ID) } returns Optional.of(user)
            every { user.userRooms } returns mutableListOf(ur1, ur2)
            every { roomInfoMapper.map(ur1) } returns dto1
            every { roomInfoMapper.map(ur2) } returns dto2

            // when
            val result = roomService.getRooms(USER_ID)

            // then
            assertThat(result).containsExactly(dto1, dto2)
        }

        @Test
        fun `티밍룸이 없으면 빈 리스트를 반환한다`() {
            // given
            val user = mockk<User>()
            every { userRepository.findById(USER_ID) } returns Optional.of(user)
            every { user.userRooms } returns mutableListOf()

            // when
            val result = roomService.getRooms(USER_ID)

            // then
            assertThat(result).isEmpty()
        }
    }

    @Nested
    @DisplayName("leaveRoom")
    inner class LeaveRoom {
        @Test
        fun `방을 떠난다`() {
            // given: user와 anotherUesr가 room에 소속되어 있는 상태
            val room = createRoom(success = true)
            val user = createUser()
            val userRoom = createUserRoom(user, room)
            user.addUserRoom(userRoom)
            room.addUserRoom(userRoom)

            every { userRoomRepository.findByRoomIdAndUserId(room.id!!, user.id!!) } returns userRoom

            // when
            roomService.leaveRoom(user.id!!, room.id!!)

            // then
            assertThat(room.userRooms).doesNotContain(userRoom)
        }

        @Test
        fun `마지막 멤버가 떠나면 방을 삭제한다`() {
            // given: 방에 멤버 1명뿐인 상태
            val room = createRoom(success = true)
            val user = mockk<User>(relaxed = true)
            val userRoom = UserRoom(user = user, room = room, roomRole = RoomRole.MEMBER)
            room.addUserRoom(userRoom)

            every { userRoomRepository.findByRoomIdAndUserId(ROOM_ID, USER_ID) } returns userRoom
            every { roomRepository.delete(room) } returns Unit

            // when
            roomService.leaveRoom(USER_ID, ROOM_ID)

            // then
            assertThat(room.userRooms).isEmpty()
            verify(exactly = 1) { roomRepository.delete(room) }
        }

        @Test
        fun `방 탈퇴 이벤트를 발생시킨다`() {
            // given
            val room = createRoom(success = true)
            val user = mockk<User>(relaxed = true)
            val userRoom = UserRoom(user = user, room = room, roomRole = RoomRole.MEMBER)
            room.addUserRoom(userRoom)

            every { userRoomRepository.findByRoomIdAndUserId(ROOM_ID, USER_ID) } returns userRoom
            every { roomRepository.delete(room) } returns Unit

            // when
            roomService.leaveRoom(USER_ID, ROOM_ID)

            // then
            verify(exactly = 1) { eventPublisher.publishEvent(ofType<RoomLeaveEvent>()) }
        }

        @Test
        fun `티밍룸을 찾지 못하면 예외를 던진다`() {
            // given
            every { userRoomRepository.findByRoomIdAndUserId(ROOM_ID, USER_ID) } returns null

            // when & then
            assertThatThrownBy { roomService.leaveRoom(USER_ID, ROOM_ID) }
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun `팀플 성공 전이라면 예외를 던진다`() {
            // given
            val user = createUser()
            val room = createRoom(success = false)
            val userRoom = createUserRoom(user, room)

            every { userRoomRepository.findByRoomIdAndUserId(room.id!!, user.id!!) } returns userRoom

            // when & then
            assertThatThrownBy { roomService.leaveRoom(user.id!!, room.id!!) }
                .isInstanceOf(IllegalStateException::class.java)
        }
    }

    @Nested
    @DisplayName("setSuccess")
    inner class SetSuccess {
        @Test
        fun `티밍룸의 상태를 '팀플 성공'으로 만든다`() {
            // given
            val room = createRoom(success = false, memberCount = 1)
            val user = mockk<User>(relaxed = true)
            val userRoom = UserRoom(user = user, room = room, roomRole = RoomRole.LEADER)
            room.addUserRoom(userRoom)

            every { userRoomRepository.findByRoomIdAndUserId(ROOM_ID, USER_ID) } returns userRoom

            // when
            roomService.setSuccess(USER_ID, ROOM_ID)

            // then
            assertThat(room.success).isTrue()
        }

        @Test
        fun `방을 찾지 못하면 예외를 던진다`() {
            every { userRoomRepository.findByRoomIdAndUserId(ROOM_ID, USER_ID) } returns null

            assertThatThrownBy { roomService.setSuccess(USER_ID, ROOM_ID) }
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun `팀장이 아니면 예외를 던진다`() {
            // given
            val room = createRoom(success = false, memberCount = 1)
            val user = mockk<User>(relaxed = true)
            val userRoom = UserRoom(user = user, room = room, roomRole = RoomRole.MEMBER)

            every { userRoomRepository.findByRoomIdAndUserId(ROOM_ID, USER_ID) } returns userRoom

            // when & then
            assertThatThrownBy { roomService.setSuccess(USER_ID, ROOM_ID) }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage(NOT_LEADER)
        }

        @Test
        fun `모든 인원이 방에 참여해 있지 않다면 예외를 던진다`() {
            // given
            val room = mockk<Room>()
            val user = mockk<User>()
            val userRoom = UserRoom(user = user, room = room, roomRole = RoomRole.LEADER)

            every { userRoomRepository.findByRoomIdAndUserId(ROOM_ID, USER_ID) } returns userRoom
            every { room.everyMemberEntered() } returns false

            // when & then
            assertThatThrownBy { roomService.setSuccess(USER_ID, ROOM_ID) }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage(EVERY_MEMBER_NOT_ENTERED)
        }
    }

    private fun getRoomCreateDto() = RoomCreateRequestDto(
        title = TITLE,
        description = DESCRIPTION,
        memberCount = MEMBER_COUNT,
        roomType = ROOM_TYPE,
        avatarKey = null,
        avatarVersion = 0
    )
}
