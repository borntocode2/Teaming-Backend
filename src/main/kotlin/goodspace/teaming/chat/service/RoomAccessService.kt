package goodspace.teaming.chat.service

import goodspace.teaming.global.exception.NOT_MEMBER_OF_ROOM
import goodspace.teaming.global.repository.UserRoomRepository
import org.springframework.stereotype.Service


@Service
class RoomAccessService(
    private val userRoomRepository: UserRoomRepository
) : RoomAccessAuthorizer {
    override fun assertMemberOf(roomId: Long, userId: Long) {
        require(userRoomRepository.existsByRoomIdAndUserId(roomId, userId)) { NOT_MEMBER_OF_ROOM }
    }
}
