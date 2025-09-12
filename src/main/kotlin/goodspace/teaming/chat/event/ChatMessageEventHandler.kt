package goodspace.teaming.chat.event

import goodspace.teaming.chat.domain.mapper.RoomUnreadCountMapper
import goodspace.teaming.global.repository.UserRoomRepository
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ChatMessageEventHandler(
    private val messaging: SimpMessagingTemplate,
    private val userRoomRepository: UserRoomRepository,
    private val roomUnreadCountMapper: RoomUnreadCountMapper
) {
    @TransactionalEventListener(
        phase = AFTER_COMMIT
    )
    fun onCreated(event: ChatMessageCreatedEvent) {
        // 방 토픽 전송
        messaging.convertAndSend("/topic/rooms/${event.roomId}", event.payload)

        // unread 갱신을 위한 개인 큐 전송
        val userRooms = userRoomRepository.findByRoomId(event.roomId)
        for (userRoom in userRooms) {
            messaging.convertAndSendToUser(
                userRoom.user.id!!.toString(), // user
                "/queue/room-events", // destination
                roomUnreadCountMapper.map(userRoom) // payload
            )
        }
    }
}
