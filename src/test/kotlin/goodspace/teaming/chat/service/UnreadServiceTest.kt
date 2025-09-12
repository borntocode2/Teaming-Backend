package goodspace.teaming.chat.service

import goodspace.teaming.chat.domain.mapper.RoomUnreadCountMapper
import goodspace.teaming.chat.dto.RoomUnreadCountResponseDto
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

private const val USER_ID = 10L
private const val ROOM_ID = 100L

class UnreadServiceTest {
    private val userRoomRepository = mockk<UserRoomRepository>(relaxed = true)
    private val messageRepository = mockk<MessageRepository>(relaxed = true)
    private val roomUnreadCountMapper = mockk<RoomUnreadCountMapper>(relaxed = true)
    private val unreadService = UnreadServiceImpl(userRoomRepository, messageRepository, roomUnreadCountMapper)

    @BeforeEach
    fun setUp() {
        clearMocks(userRoomRepository, messageRepository, roomUnreadCountMapper)
    }

    @Nested
    @DisplayName("getUnreadCounts")
    inner class GetUnreadCounts {
        @Test
        fun `사용자의 모든 티밍룸에 대한 정보를 DTO로 변환해 반환한다`() {
            // given
            val userRoom1 = userRoom(lastReadId = 5L)
            val userRoom2 = userRoom(lastReadId = 7L)
            every { userRoomRepository.findByUserId(USER_ID) } returns listOf(userRoom1, userRoom2)

            val dto1 = mockk<RoomUnreadCountResponseDto>()
            val dto2 = mockk<RoomUnreadCountResponseDto>()
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
        fun `요청 값이 null일 경우, 최신 메시지까지 읽은 것으로 판단해 포인터를 상승시킨다`() {
            // given
            val currentReadId = 5L
            val latestId = 10L

            val userRoomBeforeRaise = userRoom(lastReadId = currentReadId)
            val userRoomAfterRaise  = userRoom(lastReadId = latestId)

            every { userRoomRepository.findByRoomIdAndUserId(ROOM_ID, USER_ID) } returns userRoomBeforeRaise
            every { messageRepository.findLatestMessageId(userRoomBeforeRaise.room) } returns latestId
            every { userRoomRepository.raiseLastReadMessageId(USER_ID, ROOM_ID, latestId) } returns 1
            every { userRoomRepository.findByRoomIdAndUserId(ROOM_ID, USER_ID) } returnsMany listOf(userRoomBeforeRaise, userRoomAfterRaise)

            val dto = mockk<RoomUnreadCountResponseDto>()
            every { roomUnreadCountMapper.map(userRoomAfterRaise) } returns dto

            // when
            val result = unreadService.markRead(USER_ID, ROOM_ID, lastReadMessageId = null)

            // then
            assertThat(result).isSameAs(dto)
            verify(exactly = 1) { userRoomRepository.raiseLastReadMessageId(USER_ID, ROOM_ID, latestId) }
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
