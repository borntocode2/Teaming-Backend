package goodspace.teaming.chat.domain.mapper

import goodspace.teaming.chat.dto.LastMessagePreviewResponseDto
import goodspace.teaming.chat.dto.RoomInfoResponseDto
import goodspace.teaming.global.entity.room.Message
import goodspace.teaming.global.entity.room.Room
import goodspace.teaming.global.entity.room.RoomType
import goodspace.teaming.global.entity.room.UserRoom
import goodspace.teaming.global.entity.user.User
import goodspace.teaming.global.repository.MessageRepository
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

private const val TITLE = "프로젝트 A"
private const val IMAGE_KEY = "images/123.png"
private const val IMAGE_VERSION = 10
private const val MEMBER_COUNT = 5
private val ROOM_TYPE = RoomType.BASIC
private const val ROOM_ID = 100L
private const val LAST_READ_MESSAGE_ID = 777L
private const val UNREAD_COUNT = 7L
private const val SUCCESS = false

class RoomInfoMapperTest {

    private lateinit var messageRepository: MessageRepository
    private lateinit var lastMessagePreviewMapper: LastMessagePreviewMapper
    private lateinit var roomInfoMapper: RoomInfoMapper

    @BeforeEach
    fun setUp() {
        messageRepository = mockk(relaxed = true)
        lastMessagePreviewMapper = mockk(relaxed = true)
        roomInfoMapper = RoomInfoMapper(messageRepository, lastMessagePreviewMapper)
    }

    @Test
    fun `lastReadMessageId가 있으면, 마지막 메시지에 대한 DTO를 포함해 매핑한다`() {
        // given
        val room = mockRoom()
        val user = mockk<User>(relaxed = true)
        val userRoom = mockk<UserRoom>(relaxed = true) {
            every { this@mockk.room } returns room
            every { this@mockk.user } returns user
            every { this@mockk.lastReadMessageId } returns LAST_READ_MESSAGE_ID
        }

        val message = mockk<Message>()
        val previewDto = mockk<LastMessagePreviewResponseDto>()

        every {
            messageRepository.countUnreadInRoom(room = room, user = user, lastReadMessageId = LAST_READ_MESSAGE_ID)
        } returns UNREAD_COUNT

        every { messageRepository.findById(LAST_READ_MESSAGE_ID) } returns Optional.of(message)
        every { lastMessagePreviewMapper.map(message) } returns previewDto

        // when
        val result: RoomInfoResponseDto = roomInfoMapper.map(userRoom)

        // then
        assertThat(result.roomId).isEqualTo(ROOM_ID)
        assertThat(result.unreadCount).isEqualTo(UNREAD_COUNT)
        assertThat(result.lastMessage).isSameAs(previewDto)
        assertThat(result.title).isEqualTo(TITLE)
        assertThat(result.imageKey).isEqualTo(IMAGE_KEY)
        assertThat(result.imageVersion).isEqualTo(IMAGE_VERSION)
        assertThat(result.type).isEqualTo(ROOM_TYPE)
        assertThat(result.memberCount).isEqualTo(MEMBER_COUNT)
        assertThat(result.success).isEqualTo(SUCCESS)
    }

    @Test
    fun `lastReadMessageId가 null이면, 마지막 메시지에 대한 DTO는 null이 된다`() {
        // given
        val room = mockRoom()
        val user = mockk<User>(relaxed = true)
        val userRoom = mockk<UserRoom>(relaxed = true) {
            every { this@mockk.room } returns room
            every { this@mockk.user } returns user
            every { this@mockk.lastReadMessageId } returns null
        }

        // when
        val result = roomInfoMapper.map(userRoom)

        // then
        assertThat(result.lastMessage).isNull()
    }

    @Test
    fun `마지맛 메시지가 null이더라도 나머지 속성은 정상적으로 매핑된다`() {
        // given
        val room = mockRoom()
        val user = mockk<User>(relaxed = true)
        val userRoom = mockk<UserRoom>(relaxed = true) {
            every { this@mockk.room } returns room
            every { this@mockk.user } returns user
            every { this@mockk.lastReadMessageId } returns LAST_READ_MESSAGE_ID
        }

        every { messageRepository.countUnreadInRoom(room, user, LAST_READ_MESSAGE_ID) } returns UNREAD_COUNT
        every { messageRepository.findById(LAST_READ_MESSAGE_ID) } returns Optional.empty()

        // when
        val result = roomInfoMapper.map(userRoom)

        // then
        assertThat(result.lastMessage).isNull()
        assertThat(result.roomId).isEqualTo(ROOM_ID)
        assertThat(result.unreadCount).isEqualTo(UNREAD_COUNT)
        assertThat(result.title).isEqualTo(TITLE)
        assertThat(result.imageKey).isEqualTo(IMAGE_KEY)
        assertThat(result.imageVersion).isEqualTo(IMAGE_VERSION)
        assertThat(result.type).isEqualTo(ROOM_TYPE)
        assertThat(result.memberCount).isEqualTo(MEMBER_COUNT)
        assertThat(result.success).isEqualTo(SUCCESS)
    }

    private fun mockRoom(): Room = mockk<Room> {
        every { id } returns ROOM_ID
        every { title } returns TITLE
        every { imageKey } returns IMAGE_KEY
        every { imageVersion } returns IMAGE_VERSION
        every { type } returns ROOM_TYPE
        every { memberCount } returns MEMBER_COUNT
        every { success } returns SUCCESS
    }
}
