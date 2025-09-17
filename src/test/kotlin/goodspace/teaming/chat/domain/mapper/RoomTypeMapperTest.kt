package goodspace.teaming.chat.domain.mapper

import goodspace.teaming.chat.dto.RoomTypeResponseDto
import goodspace.teaming.global.entity.room.RoomType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class RoomTypeMapperTest {

    private lateinit var roomTypeMapper: RoomTypeMapper

    @BeforeEach
    fun setUp() {
        roomTypeMapper = RoomTypeMapper()
    }

    @ParameterizedTest
    @EnumSource(RoomType::class)
    fun `RoomType을 DTO로 변환한다`(roomType: RoomType) {
        // when
        val result: RoomTypeResponseDto = roomTypeMapper.map(roomType)

        // then
        assertThat(result.typeName).isEqualTo(roomType.typeName)
        assertThat(result.price).isEqualTo(roomType.price)
        assertThat(result.description).isEqualTo(roomType.description)
    }
}
