package goodspace.teaming.chat.dto

data class ReadBoundaryUpdateResponseDto(
    val userId: Long,
    val lastReadMessageId: Long?,
    val unreadCount: Long
)
