package goodspace.teaming.chat.domain.mapper

import goodspace.teaming.chat.dto.LastMessagePreviewResponseDto
import goodspace.teaming.chat.dto.RoomInfoResponseDto
import goodspace.teaming.chat.dto.RoomTypeResponseDto
import goodspace.teaming.chat.dto.SenderSummaryResponseDto
import goodspace.teaming.file.domain.CdnStorageUrlProvider
import goodspace.teaming.fixture.MESSAGE_CONTENT
import goodspace.teaming.global.entity.room.*
import goodspace.teaming.global.entity.user.User
import goodspace.teaming.global.repository.MessageRepository
import goodspace.teaming.util.createRoom
import goodspace.teaming.util.createUser
import goodspace.teaming.util.createUserRoom
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
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
private const val AVATAR_URL = "avatarUrl"
private const val LATEST_MESSAGE_ID = 92039103L

class RoomInfoMapperTest {
    private lateinit var messageRepository: MessageRepository
    private lateinit var lastMessagePreviewMapper: LastMessagePreviewMapper
    private lateinit var roomMemberMapper: RoomMemberMapper
    private lateinit var roomTypeMapper: RoomTypeMapper
    private lateinit var storageUrlProvider: CdnStorageUrlProvider

    private lateinit var roomInfoMapper: RoomInfoMapper

    @BeforeEach
    fun setUp() {
        messageRepository = mockk(relaxed = true)
        lastMessagePreviewMapper = mockk(relaxed = true)
        roomMemberMapper = mockk(relaxed = true)
        roomTypeMapper = mockk(relaxed = true)
        storageUrlProvider = mockk(relaxed = true)
        roomInfoMapper = RoomInfoMapper(
            messageRepository,
            lastMessagePreviewMapper,
            roomMemberMapper,
            roomTypeMapper,
            storageUrlProvider
        )
    }

    @Test
    fun `lastReadMessageId가 있으면, 마지막 메시지에 대한 DTO를 포함해 매핑한다`() {
        // given
        val room = createRoom()
        val user = createUser()
        val userRoom = createUserRoom(user, room, lastReadMessageId = LAST_READ_MESSAGE_ID)

        val message = mockk<Message>()
        val expectedPreviewDto = createLastMessagePreviewResponseDto()
        val expectedRoomTypeDto = mockk<RoomTypeResponseDto>()

        every {
            messageRepository.countUnreadInRoom(
                room = room,
                user = user,
                lastReadMessageId = userRoom.lastReadMessageId
            )
        } returns UNREAD_COUNT

        every { messageRepository.findLatestMessageId(room) } returns LATEST_MESSAGE_ID
        every { messageRepository.findById(LATEST_MESSAGE_ID) } returns Optional.of(message)
        every { storageUrlProvider.publicUrl(room.avatarKey, room.avatarVersion) } returns AVATAR_URL
        every { lastMessagePreviewMapper.map(message) } returns expectedPreviewDto
        every { roomTypeMapper.map(room.type) } returns expectedRoomTypeDto

        // when
        val result: RoomInfoResponseDto = roomInfoMapper.map(userRoom)

        // then
        assertThat(result.roomId).isEqualTo(room.id)
        assertThat(result.unreadCount).isEqualTo(UNREAD_COUNT)
        assertThat(result.lastMessage).isEqualTo(expectedPreviewDto)
        assertThat(result.title).isEqualTo(room.title)
        assertThat(result.avatarUrl).isEqualTo(AVATAR_URL)
        assertThat(result.avatarVersion).isEqualTo(room.avatarVersion)
        assertThat(result.type).isEqualTo(expectedRoomTypeDto)
        assertThat(result.memberCount).isEqualTo(room.memberCount)
        assertThat(result.success).isEqualTo(room.success)
    }

    @Test
    fun `lastMessageId가 null이면, 마지막 메시지에 대한 DTO는 null이 된다`() {
        // given
        val room = mockRoom()
        val user = mockk<User>(relaxed = true)
        val userRoom = mockk<UserRoom>(relaxed = true) {
            every { this@mockk.room } returns room
            every { this@mockk.user } returns user
            every { this@mockk.lastReadMessageId } returns null
        }

        every { messageRepository.findLatestMessageId(room) } returns null

        // when
        val result = roomInfoMapper.map(userRoom)

        // then
        assertThat(result.lastMessage).isNull()
    }

    @Test
    fun `마지막 메시지가 null이더라도 나머지 속성은 정상적으로 매핑된다`() {
        // given
        val room = createRoom()
        val user = createUser()
        val userRoom = createUserRoom(user, room, lastReadMessageId = null)
        val expectedRoomTypeDto = mockk<RoomTypeResponseDto>()

        every { messageRepository.countUnreadInRoom(room, user, null) } returns UNREAD_COUNT
        every { messageRepository.findLatestMessageId(room) } returns null
        every { messageRepository.findById(LAST_READ_MESSAGE_ID) } returns Optional.empty()
        every { storageUrlProvider.publicUrl(room.avatarKey, room.avatarVersion) } returns AVATAR_URL
        every { roomTypeMapper.map(room.type) } returns expectedRoomTypeDto

        // when
        val result = roomInfoMapper.map(userRoom)

        // then
        assertThat(result.lastMessage).isNull()
        assertThat(result.roomId).isEqualTo(room.id)
        assertThat(result.unreadCount).isEqualTo(UNREAD_COUNT)
        assertThat(result.title).isEqualTo(room.title)
        assertThat(result.avatarUrl).isEqualTo(AVATAR_URL)
        assertThat(result.avatarVersion).isEqualTo(room.avatarVersion)
        assertThat(result.type).isEqualTo(expectedRoomTypeDto)
        assertThat(result.memberCount).isEqualTo(room.memberCount)
        assertThat(result.success).isEqualTo(room.success)
    }

    private fun mockRoom(): Room = mockk<Room> {
        every { id } returns ROOM_ID
        every { title } returns TITLE
        every { avatarKey } returns IMAGE_KEY
        every { avatarVersion } returns IMAGE_VERSION
        every { type } returns ROOM_TYPE
        every { memberCount } returns MEMBER_COUNT
        every { success } returns SUCCESS
        every { userRooms } returns mutableListOf()
    }

    private fun createLastMessagePreviewResponseDto(
        id: Long = LAST_READ_MESSAGE_ID,
        type: MessageType = MessageType.TEXT,
        content: String = MESSAGE_CONTENT,
        sender: SenderSummaryResponseDto = mockk(),
        createdAt: Instant = Instant.now()
    ): LastMessagePreviewResponseDto {
        return LastMessagePreviewResponseDto(
            id = id,
            type = type,
            content = content,
            sender = sender,
            createdAt = createdAt
        )
    }
}
