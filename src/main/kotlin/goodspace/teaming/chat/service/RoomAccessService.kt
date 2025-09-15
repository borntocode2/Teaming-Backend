package goodspace.teaming.chat.service

import goodspace.teaming.global.repository.UserRoomRepository
import org.springframework.stereotype.Service

private const val USER_ROOM_NOT_FOUND = "해당 방에 소속되지 않았습니다."

@Service
class RoomAccessService(
    private val userRoomRepository: UserRoomRepository
) : RoomAccessAuthorizer {
    override fun assertMemberOf(roomId: Long, userId: Long) {
        require(userRoomRepository.existsByRoomIdAndUserId(roomId, userId)) { USER_ROOM_NOT_FOUND }
    }
}
