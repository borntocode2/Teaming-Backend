package goodspace.teaming.chat.dto

data class RoomUnreadCountResponseDto(
    val roomId: Long,
    val unreadCount: Long,
    val lastMessage: LastMessagePreviewResponseDto?
)
