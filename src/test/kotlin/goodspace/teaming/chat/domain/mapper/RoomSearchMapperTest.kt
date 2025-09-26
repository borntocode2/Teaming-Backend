package goodspace.teaming.chat.domain.mapper

import goodspace.teaming.chat.dto.RoomSearchResponseDto
import goodspace.teaming.chat.dto.RoomTypeResponseDto
import goodspace.teaming.file.domain.CdnStorageUrlProvider
import goodspace.teaming.global.entity.room.Room
import goodspace.teaming.global.entity.room.RoomType
import goodspace.teaming.global.entity.room.UserRoom
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private const val TITLE = "Study Group"
private const val DESCRIPTION = "Description"
private const val AVATAR_KEY = "room-image-key"
private const val AVATAR_VERSION = 3
private const val AVATAR_URL = "avatarUrl"
private const val MAX_MEMBER_COUNT = 10
private const val CURRENT_MEMBER_COUNT = 3

private val ROOM_TYPE = RoomType.BASIC
private val ROOM_TYPE_DTO = RoomTypeResponseDto(
    typeName = "Basic Room",
    price = 2060,
    description = "테스트용 BASIC DTO"
)

class RoomSearchMapperTest {
    private lateinit var roomTypeMapper: RoomTypeMapper
    private lateinit var urlProvider: CdnStorageUrlProvider
    private lateinit var roomSearchMapper: RoomSearchMapper

    @BeforeEach
    fun setUp() {
        roomTypeMapper = mockk()
        urlProvider = mockk(relaxed = true)
        roomSearchMapper = RoomSearchMapper(roomTypeMapper, urlProvider)
    }

    @Test
    fun `티밍룸의 정보를 기반으로 DTO를 생성한다`() {
        // given
        val room = createRoomWithMembers(
            title = TITLE,
            avatarKey = AVATAR_KEY,
            avatarVersion = AVATAR_VERSION,
            type = ROOM_TYPE,
            maxMemberCount = MAX_MEMBER_COUNT,
            currentCount = CURRENT_MEMBER_COUNT
        )
        every { roomTypeMapper.map(ROOM_TYPE) } returns ROOM_TYPE_DTO
        every { urlProvider.publicUrl(room.avatarKey, room.avatarVersion) } returns AVATAR_URL

        // when
        val result: RoomSearchResponseDto = roomSearchMapper.map(room)

        // then
        assertThat(result.title).isEqualTo(room.title)
        assertThat(result.avatarUrl).isEqualTo(AVATAR_URL)
        assertThat(result.avatarVersion).isEqualTo(room.avatarVersion)
        assertThat(result.type).isEqualTo(ROOM_TYPE_DTO)
        assertThat(result.currentMemberCount).isEqualTo(room.currentMemberCount())
        assertThat(result.maxMemberCount).isEqualTo(room.memberCount)
    }

    @Test
    fun `티밍룸 타입 DTO의 생성은 다른 매퍼에게 위임한다`() {
        val room = createRoomWithMembers(type = ROOM_TYPE)

        every { roomTypeMapper.map(ROOM_TYPE) } returns ROOM_TYPE_DTO

        // when
        roomSearchMapper.map(room)

        // then
        verify(exactly = 1) { roomTypeMapper.map(ROOM_TYPE) }
    }

    private fun createRoomWithMembers(
        title: String = TITLE,
        description: String = DESCRIPTION,
        avatarKey: String? = AVATAR_KEY,
        avatarVersion: Int = AVATAR_VERSION,
        type: RoomType = ROOM_TYPE,
        maxMemberCount: Int = MAX_MEMBER_COUNT,
        currentCount: Int = CURRENT_MEMBER_COUNT
    ): Room {
        val room = Room(
            title = title,
            description = description,
            avatarKey = avatarKey,
            avatarVersion = avatarVersion,
            type = type,
            memberCount = maxMemberCount
        )
        repeat(currentCount) {
            room.addUserRoom(mockk<UserRoom>())
        }

        return room
    }
}
