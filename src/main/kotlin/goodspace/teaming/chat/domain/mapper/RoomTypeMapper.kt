package goodspace.teaming.chat.domain.mapper

import goodspace.teaming.chat.dto.RoomTypeResponseDto
import goodspace.teaming.global.entity.room.RoomType
import org.springframework.stereotype.Component

@Component
class RoomTypeMapper {
    fun map(roomType: RoomType): RoomTypeResponseDto {
        return RoomTypeResponseDto(
            typeName = roomType.typeName,
            price = roomType.price,
            description = roomType.description
        )
    }
}
