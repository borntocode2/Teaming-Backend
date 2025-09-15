package goodspace.teaming.chat.event

data class ReadBoundaryUpdateEvent(
    val roomId: Long,
    val userId: Long,
    val lastReadMessageId: Long?,
    val unreadCount: Long
)
