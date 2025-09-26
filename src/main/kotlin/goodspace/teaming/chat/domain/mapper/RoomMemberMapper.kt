package goodspace.teaming.chat.domain.mapper

import goodspace.teaming.chat.dto.RoomMemberResponseDto
import goodspace.teaming.file.domain.CdnStorageUrlProvider
import goodspace.teaming.global.entity.room.UserRoom
import org.springframework.stereotype.Component

@Component
class RoomMemberMapper(
    private val urlProvider: CdnStorageUrlProvider
) {
    fun map(userRoom: UserRoom): RoomMemberResponseDto {
        val user = userRoom.user

        return RoomMemberResponseDto(
            memberId = user.id!!,
            lastReadMessageId = userRoom.lastReadMessageId,
            name = user.name,
            avatarUrl = urlProvider.publicUrl(user.avatarKey, user.avatarVersion),
            avatarVersion = user.avatarVersion,
            roomRole = userRoom.roomRole
        )
    }
}
