package goodspace.teaming.chat.domain.mapper

import goodspace.teaming.chat.dto.RoomCreateRequestDto
import goodspace.teaming.global.entity.room.Room
import goodspace.teaming.global.entity.room.RoomType
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

private const val TITLE = "default title"
private const val MEMBER_COUNT = 10
private val ROOM_TYPE = RoomType.BASIC
private const val IMAGE_KEY = "images/123.png"
private const val IMAGE_VERSION = 10

class RoomMapperTest {
    private val roomMapper = RoomMapper()

    @Test
    fun `모든 매핑 가능한 필드를 정확히 복사한다`() {
        // given
        val dto = createDto(
            title = TITLE,
            memberCount = MEMBER_COUNT,
            roomType = ROOM_TYPE,
            imageKey = IMAGE_KEY,
            imageVersion = IMAGE_VERSION
        )

        // when
        val room: Room = roomMapper.map(dto)

        // then
        assertThat(room.title).isEqualTo(dto.title)
        assertThat(room.type).isEqualTo(dto.roomType)
        assertThat(room.memberCount).isEqualTo(dto.memberCount)
        assertThat(room.avatarKey).isEqualTo(dto.avatarKey)
        assertThat(room.avatarVersion).isEqualTo(dto.avatarVersion)
    }

    @Test
    fun `옵셔널한 속성은 null 값을 허용한다`() {
        // given
        val dto = createDto(
            imageKey = null,
        )

        // when
        val room = roomMapper.map(dto)

        // then
        assertThat(room.avatarKey).isNull()
    }

    private fun createDto(
        title: String = "기본 타이틀",
        description: String = "기본 설명",
        memberCount: Int = 10,
        roomType: RoomType = RoomType.BASIC,
        imageKey: String? = "default/key",
        imageVersion: Int = 1
    ): RoomCreateRequestDto {
        return RoomCreateRequestDto(
            title = title,
            description = description,
            memberCount = memberCount,
            roomType = roomType,
            avatarKey = imageKey,
            avatarVersion = imageVersion
        )
    }
}
