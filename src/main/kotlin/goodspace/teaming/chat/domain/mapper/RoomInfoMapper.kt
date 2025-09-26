package goodspace.teaming.chat.domain.mapper

import goodspace.teaming.chat.dto.RoomInfoResponseDto
import goodspace.teaming.file.domain.CdnStorageUrlProvider
import goodspace.teaming.global.entity.room.UserRoom
import goodspace.teaming.global.repository.MessageRepository
import org.springframework.stereotype.Component

@Component
class RoomInfoMapper(
    private val messageRepository: MessageRepository,
    private val lastMessagePreviewMapper: LastMessagePreviewMapper,
    private val roomMemberMapper: RoomMemberMapper,
    private val roomTypeMapper: RoomTypeMapper,
    private val storageUrlProvider: CdnStorageUrlProvider
)  {
    fun map(userRoom: UserRoom): RoomInfoResponseDto {
        val lastReadMessageId = userRoom.lastReadMessageId

        val unreadCount = messageRepository.countUnreadInRoom(
            room = userRoom.room,
            user = userRoom.user,
            lastReadMessageId = lastReadMessageId
        )

        val lastMessageId = messageRepository.findLatestMessageId(userRoom.room)

        val lastMessageDto = lastMessageId
            ?.let { messageRepository.findById(it).orElse(null) }
            ?.let { lastMessagePreviewMapper.map(it) }

        val room = userRoom.room
        val members = room.userRooms

        return RoomInfoResponseDto(
            roomId = room.id!!,
            role = userRoom.roomRole,
            unreadCount = unreadCount,
            lastMessage = lastMessageDto,
            title = room.title,
            avatarUrl = storageUrlProvider.publicUrl(room.avatarKey, room.avatarVersion),
            avatarVersion = room.avatarVersion,
            type = roomTypeMapper.map(room.type),
            memberCount = room.memberCount,
            paymentStatus = userRoom.paymentStatus,
            success = room.success,
            members = members.map { roomMemberMapper.map(it) }
        )
    }
}
