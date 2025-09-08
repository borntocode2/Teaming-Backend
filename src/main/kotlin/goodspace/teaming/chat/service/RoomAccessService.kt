package goodspace.teaming.chat.service

import goodspace.teaming.global.repository.UserRoomRepository
import org.springframework.stereotype.Service

@Service
class RoomAccessService(
    private val userRoomRepository: UserRoomRepository
) : RoomAccessAuthorizer {
    override fun assertMemberOf(roomId: Long, userId: Long) {
        require(userRoomRepository.existsByRoomIdAndUserId(roomId, userId)) { "해당 방에 대한 구독 권한이 없습니다." }
    }
}
