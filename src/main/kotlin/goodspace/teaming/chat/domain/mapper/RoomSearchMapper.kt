package goodspace.teaming.chat.domain.mapper

import goodspace.teaming.chat.dto.RoomSearchResponseDto
import goodspace.teaming.global.entity.room.Room
import org.springframework.stereotype.Component

@Component
class RoomSearchMapper(
    private val roomTypeMapper: RoomTypeMapper
) {
    fun map(room: Room): RoomSearchResponseDto {
        return RoomSearchResponseDto(
            title = room.title,
            imageKey = room.imageKey,
            imageVersion = room.imageVersion,
            type = roomTypeMapper.map(room.type),
            currentMemberCount = room.currentMemberCount(),
            maxMemberCount = room.memberCount
        )
    }
}
