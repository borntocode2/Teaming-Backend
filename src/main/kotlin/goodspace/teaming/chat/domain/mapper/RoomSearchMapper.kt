package goodspace.teaming.chat.domain.mapper

import goodspace.teaming.chat.dto.RoomSearchResponseDto
import goodspace.teaming.file.domain.CdnStorageUrlProvider
import goodspace.teaming.global.entity.room.Room
import org.springframework.stereotype.Component

@Component
class RoomSearchMapper(
    private val roomTypeMapper: RoomTypeMapper,
    private val storageUrlProvider: CdnStorageUrlProvider
) {
    fun map(room: Room): RoomSearchResponseDto {
        return RoomSearchResponseDto(
            title = room.title,
            avatarUrl = storageUrlProvider.publicUrl(room.avatarKey, room.avatarVersion),
            avatarVersion = room.avatarVersion,
            type = roomTypeMapper.map(room.type),
            currentMemberCount = room.currentMemberCount,
            maxMemberCount = room.memberCount
        )
    }
}
