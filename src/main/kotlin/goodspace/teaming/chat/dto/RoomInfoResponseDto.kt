package goodspace.teaming.chat.dto

import goodspace.teaming.global.entity.room.RoomType

data class RoomInfoResponseDto(
    val roomId: Long,
    val unreadCount: Long,
    val lastMessage: LastMessagePreviewResponseDto?,
    val title: String,
    val imageKey: String?,
    val imageVersion: Int?,
    val type: RoomType,
    val memberCount: Int,
    val success: Boolean,
    val members: List<RoomMemberResponseDto>
)
