package goodspace.teaming.chat.service

import goodspace.teaming.chat.dto.RoomUnreadCountResponseDto

interface UnreadService {
    fun getUnreadCounts(userId: Long): List<RoomUnreadCountResponseDto>
    fun markRead(userId: Long, roomId: Long, lastReadMessageId: Long?): RoomUnreadCountResponseDto
}
