package goodspace.teaming.chat.dto

import goodspace.teaming.global.entity.room.RoomRole

data class RoomMemberResponseDto(
    val memberId: Long,
    val lastReadMessageId: Long?,
    val name: String,
    val avatarUrl: String?,
    val avatarVersion: Int?,
    val roomRole: RoomRole
)
