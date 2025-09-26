package goodspace.teaming.chat.domain.mapper

import goodspace.teaming.chat.dto.RoomCreateRequestDto
import goodspace.teaming.global.entity.room.Room
import org.springframework.stereotype.Component

@Component
class RoomMapper {
    fun map(dto: RoomCreateRequestDto): Room {
        return Room(
            title = dto.title,
            avatarKey = dto.avatarKey,
            avatarVersion = dto.avatarVersion,
            description = dto.description,
            type = dto.roomType,
            memberCount = dto.memberCount
        )
    }
}
