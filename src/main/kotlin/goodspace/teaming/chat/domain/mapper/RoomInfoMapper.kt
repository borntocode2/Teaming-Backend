package goodspace.teaming.chat.domain.mapper

import goodspace.teaming.chat.dto.RoomInfoResponseDto
import goodspace.teaming.global.entity.room.UserRoom
import goodspace.teaming.global.repository.MessageRepository

class RoomInfoMapper(
    private val messageRepository: MessageRepository,
    private val lastMessagePreviewMapper: LastMessagePreviewMapper
)  {
    fun map(userRoom: UserRoom): RoomInfoResponseDto {
        val lastReadMessageId = userRoom.lastReadMessageId

        val unreadCount = messageRepository.countUnreadInRoom(
            room = userRoom.room,
            user = userRoom.user,
            lastReadMessageId = lastReadMessageId
        )

        val lastMessageDto = lastReadMessageId
            ?.let(messageRepository::findById)
            ?.map { lastMessagePreviewMapper.map(it) }
            ?.orElse(null)

        val room = userRoom.room

        return RoomInfoResponseDto(
            roomId = room.id!!,
            unreadCount = unreadCount,
            lastMessage = lastMessageDto,
            title = room.title,
            imageKey = room.imageKey,
            imageVersion = room.imageVersion,
            type = room.type,
            memberCount = room.memberCount,
            success = room.success
        )
    }
}
