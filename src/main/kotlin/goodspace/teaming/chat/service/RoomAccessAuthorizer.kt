package goodspace.teaming.chat.service

interface RoomAccessAuthorizer {
    fun assertMemberOf(roomId: Long, userId: Long)
}
