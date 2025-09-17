package goodspace.teaming.chat.service

import goodspace.teaming.chat.domain.mapper.RoomUnreadCountMapper
import goodspace.teaming.chat.dto.RoomUnreadCountResponseDto
import goodspace.teaming.chat.event.ReadBoundaryUpdatedEvent
import goodspace.teaming.global.entity.room.Room
import goodspace.teaming.global.entity.room.UserRoom
import goodspace.teaming.global.entity.user.User
import goodspace.teaming.global.repository.MessageRepository
import goodspace.teaming.global.repository.UserRoomRepository
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import org.springframework.test.context.event.RecordApplicationEvents

private const val USER_ID = 10L
private const val ROOM_ID = 100L

@RecordApplicationEvents
class UnreadServiceTest {
    private val userRoomRepository = mockk<UserRoomRepository>(relaxed = true)
    private val messageRepository = mockk<MessageRepository>(relaxed = true)
    private val roomUnreadCountMapper = mockk<RoomUnreadCountMapper>(relaxed = true)
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

    private val unreadService = UnreadServiceImpl(
        userRoomRepository = userRoomRepository,
        messageRepository = messageRepository,
        roomUnreadCountMapper = roomUnreadCountMapper,
        eventPublisher = eventPublisher
    )

    @BeforeEach
    fun setUp() {
        clearMocks(userRoomRepository, messageRepository, roomUnreadCountMapper, eventPublisher)
    }

    @Nested
    @DisplayName("getUnreadCounts")
    inner class GetUnreadCounts {
        @Test
        fun `사용자의 모든 티밍룸에 대한 정보를 DTO로 변환해 반환한다`() {
            // given
            val userRoom1 = userRoom(5L)
            val userRoom2 = userRoom(7L)
            val dto1 = mockk<RoomUnreadCountResponseDto>()
            val dto2 = mockk<RoomUnreadCountResponseDto>()

            every { userRoomRepository.findByUserId(USER_ID) } returns listOf(userRoom1, userRoom2)
            every { roomUnreadCountMapper.map(userRoom1) } returns dto1
            every { roomUnreadCountMapper.map(userRoom2) } returns dto2

            // when
            val result = unreadService.getUnreadCounts(USER_ID)

            // then
            assertThat(result).containsExactly(dto1, dto2)
        }
    }

    @Nested
    @DisplayName("markRead")
    inner class MarkRead {
        @Test
        fun `사용자가 해당 방에 없으면 예외가 발생한다`() {
            // given
            every { userRoomRepository.findByRoomIdAndUserId(ROOM_ID, USER_ID) } returns null

            // when & then
            assertThatThrownBy { unreadService.markRead(USER_ID, ROOM_ID, lastReadMessageId = 1L) }
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun `요청 값이 null일 경우, 최신 메시지까지 읽은 것으로 판단해 읽음 경계를 상승시킨다`() {
            // given
            val currentReadMessageId = 5L
            val latestMessageId = 10L
            val existingUserRoom = userRoom(currentReadMessageId)
            val updatedUserRoom  = userRoom(latestMessageId)

            every { userRoomRepository.findByRoomIdAndUserId(ROOM_ID, USER_ID) } returnsMany listOf(existingUserRoom, updatedUserRoom)
            every { messageRepository.findLatestMessageId(existingUserRoom.room) } returns latestMessageId
            every { userRoomRepository.raiseLastReadMessageId(USER_ID, ROOM_ID, latestMessageId) } returns 1
            every { roomUnreadCountMapper.map(updatedUserRoom) } returns mockk(relaxed = true)

            // when
            unreadService.markRead(USER_ID, ROOM_ID, lastReadMessageId = null)

            // then
            verify(exactly = 1) { userRoomRepository.raiseLastReadMessageId(USER_ID, ROOM_ID, latestMessageId) }
        }

        @Test
        fun `이미 최신 메시지까지 읽은 상태라면 읽음 경계를 상승시키지 않는다`() {
            // given
            val latestMessageId = 10L
            val userRoom = userRoom(latestMessageId)

            every { userRoomRepository.findByRoomIdAndUserId(ROOM_ID, USER_ID) } returns userRoom
            every { messageRepository.findLatestMessageId(userRoom.room) } returns latestMessageId
            every { roomUnreadCountMapper.map(userRoom) } returns mockk(relaxed = true)

            // when
            unreadService.markRead(USER_ID, ROOM_ID, lastReadMessageId = latestMessageId)

            // then
            verify(exactly = 0) { userRoomRepository.raiseLastReadMessageId(any(), any(), any()) }
        }

        @Test
        fun `읽음 경계를 상승시켰다면 이벤트를 발행한다`() {
            // given
            val currentReadMessageId = 5L
            val latestMessageId = 10L
            val existingUserRoom = userRoom(currentReadMessageId)
            val updatedUserRoom  = userRoom(latestMessageId)

            every { userRoomRepository.findByRoomIdAndUserId(ROOM_ID, USER_ID) } returnsMany listOf(existingUserRoom, updatedUserRoom)
            every { messageRepository.findLatestMessageId(existingUserRoom.room) } returns latestMessageId
            every { userRoomRepository.raiseLastReadMessageId(USER_ID, ROOM_ID, latestMessageId) } returns 1
            every { roomUnreadCountMapper.map(updatedUserRoom) } returns mockk(relaxed = true)

            // when
            unreadService.markRead(USER_ID, ROOM_ID, lastReadMessageId = null)

            // then
            verify(exactly = 1) { eventPublisher.publishEvent(any<ReadBoundaryUpdatedEvent>()) }
        }

        @Test
        fun `읽음 경계를 상승시키지 않았다면 이벤트를 발행하지 않는다`() {
            // given: 최신 메시지까지 읽은 상태
            val latestMessageId = 10L
            val userRoom = userRoom(latestMessageId)

            every { userRoomRepository.findByRoomIdAndUserId(ROOM_ID, USER_ID) } returns userRoom
            every { messageRepository.findLatestMessageId(userRoom.room) } returns latestMessageId
            every { roomUnreadCountMapper.map(userRoom) } returns mockk(relaxed = true)

            // when
            unreadService.markRead(USER_ID, ROOM_ID, lastReadMessageId = latestMessageId)

            // then
            verify(exactly = 0) { eventPublisher.publishEvent(any<ReadBoundaryUpdatedEvent>()) }
        }
    }

    private fun userRoom(lastReadId: Long?): UserRoom {
        val room = mockk<Room>(relaxed = true) { every { id } returns ROOM_ID }
        val user = mockk<User>(relaxed = true) { every { id } returns USER_ID }

        return mockk(relaxed = true) {
            every { this@mockk.room } returns room
            every { this@mockk.user } returns user
            every { lastReadMessageId } returns lastReadId
        }
    }
}
