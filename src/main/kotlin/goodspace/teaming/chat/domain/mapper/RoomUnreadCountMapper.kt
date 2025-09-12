package goodspace.teaming.chat.domain.mapper

import goodspace.teaming.chat.dto.RoomUnreadCountResponseDto
import goodspace.teaming.global.entity.room.UserRoom
import goodspace.teaming.global.repository.MessageRepository
import org.springframework.stereotype.Component

@Component
class RoomUnreadCountMapper(
    private val messageRepository: MessageRepository,
    private val lastMessagePreviewMapper: LastMessagePreviewMapper
) {
    fun map(userRoom: UserRoom): RoomUnreadCountResponseDto {
        val lastMessageId = userRoom.lastReadMessageId

        val unreadCount = messageRepository.countUnreadInRoom(
            room = userRoom.room,
            user = userRoom.user,
            lastReadMessageId = lastMessageId
        )

        val lastMessageDto = lastMessageId
            ?.let(messageRepository::findById)
            ?.map { lastMessagePreviewMapper.map(it) }
            ?.orElse(null)

        return RoomUnreadCountResponseDto(
            roomId = userRoom.room.id!!,
            unreadCount = unreadCount,
            lastMessage = lastMessageDto
        )
    }
}
