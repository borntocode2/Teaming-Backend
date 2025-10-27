package goodspace.teaming.chat.domain.mapper

import goodspace.teaming.chat.dto.RoomReadyResponseDto
import goodspace.teaming.global.entity.room.Room
import org.springframework.stereotype.Component

@Component
class RoomReadyMapper {
    fun map(room: Room): RoomReadyResponseDto {
        return RoomReadyResponseDto(
            everyMemberEntered = room.everyMemberEntered()
        )
    }

    private fun Room.everyMemberEntered(): Boolean {
        return memberCount == currentMemberCount()
    }
}
