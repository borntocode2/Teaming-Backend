package goodspace.teaming.chat.domain.mapper

import goodspace.teaming.chat.dto.LastMessagePreviewResponseDto
import goodspace.teaming.chat.dto.RoomUnreadCountResponseDto
import goodspace.teaming.global.entity.room.Message
import goodspace.teaming.global.entity.room.Room
import goodspace.teaming.global.entity.room.UserRoom
import goodspace.teaming.global.entity.user.User
import goodspace.teaming.global.repository.MessageRepository
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

private const val ROOM_ID = 40L
private const val USER_ID = 80L
private const val LAST_READ_MESSAGE_ID = 120L

class RoomUnreadCountMapperTest {
    private val messageRepository: MessageRepository = mockk(relaxed = true)
    private val lastMessagePreviewMapper: LastMessagePreviewMapper = mockk(relaxed = true)
    private lateinit var roomUnreadCountMapper: RoomUnreadCountMapper

    private val room: Room = mockk { every { id } returns ROOM_ID }
    private val user: User = mockk { every { id } returns USER_ID }
    private lateinit var userRoom: UserRoom

    @BeforeEach
    fun setUp() {
        roomUnreadCountMapper = RoomUnreadCountMapper(messageRepository, lastMessagePreviewMapper)
        userRoom = mockk {
            every { room } returns this@RoomUnreadCountMapperTest.room
            every { user } returns this@RoomUnreadCountMapperTest.user
            every { lastReadMessageId } returns LAST_READ_MESSAGE_ID
        }
    }

    @Test
    fun `해당 채팅방에 마지막 메시지가 존재하면 lastMessage 프리뷰를 포함한다`() {
        // given
        val message = mockk<Message>()
        val previewDto = mockk<LastMessagePreviewResponseDto>()
        val unreadCount = 5L

        every { messageRepository.findById(LAST_READ_MESSAGE_ID) } returns Optional.of(message)
        every { lastMessagePreviewMapper.map(message) } returns previewDto
        every { messageRepository.countUnreadInRoom(room, user, LAST_READ_MESSAGE_ID) } returns unreadCount

        // when
        val result: RoomUnreadCountResponseDto = roomUnreadCountMapper.map(userRoom)

        // then
        assertThat(result.roomId).isEqualTo(ROOM_ID)
        assertThat(result.unreadCount).isEqualTo(unreadCount)
        assertThat(result.lastMessage).isEqualTo(previewDto)
    }

    @Test
    fun `해당 채팅방에 메시지가 없으면 lastMessage는 null이 된다`() {
        // given
        val unreadCount = 0L
        every { messageRepository.findById(LAST_READ_MESSAGE_ID) } returns Optional.empty()
        every { messageRepository.countUnreadInRoom(room, user, LAST_READ_MESSAGE_ID) } returns unreadCount

        // when
        val result = roomUnreadCountMapper.map(userRoom)

        // then
        assertThat(result.roomId).isEqualTo(ROOM_ID)
        assertThat(result.unreadCount).isEqualTo(unreadCount)
        assertThat(result.lastMessage).isNull()
    }
}
