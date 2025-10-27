package goodspace.teaming.chat.event

data class RoomLeaveEvent(
    val roomId: Long,
    val memberId: Long
)
