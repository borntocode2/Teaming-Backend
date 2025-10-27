package goodspace.teaming.chat.dto

import goodspace.teaming.global.entity.room.PaymentStatus
import goodspace.teaming.global.entity.room.RoomRole

data class RoomInfoResponseDto(
    val roomId: Long,
    val role: RoomRole,
    val unreadCount: Long,
    val lastMessage: LastMessagePreviewResponseDto?,
    val title: String,
    val avatarUrl: String?,
    val avatarVersion: Int,
    val type: RoomTypeResponseDto,
    val memberCount: Int,
    val paymentStatus: PaymentStatus,
    val success: Boolean,
    val members: List<RoomMemberResponseDto>,
    val ready: RoomReadyResponseDto
)
