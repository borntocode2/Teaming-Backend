package goodspace.teaming.chat.event

data class ReadBoundaryUpdatedEvent(
    val roomId: Long,
    val userId: Long,
    val lastReadMessageId: Long?,
    val unreadCount: Long
)
