package goodspace.teaming.chat.event

import goodspace.teaming.chat.domain.mapper.RoomUnreadCountMapper
import goodspace.teaming.global.entity.room.MessageType
import goodspace.teaming.global.entity.room.UserRoom
import goodspace.teaming.global.repository.UserRoomRepository
import goodspace.teaming.push.service.PushNotificationService
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ChatMessageEventHandler(
    private val messaging: SimpMessagingTemplate,
    private val userRoomRepository: UserRoomRepository,
    private val roomUnreadCountMapper: RoomUnreadCountMapper,
    private val pushNotificationService: PushNotificationService
) {
    @TransactionalEventListener(
        phase = AFTER_COMMIT
    )
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
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

        sendPushNotification(event, userRooms)
    }

    private fun sendPushNotification(
        event: ChatMessageCreatedEvent,
        userRooms: List<UserRoom>
    ) {
        val senderId = event.senderId

        // 보낸 사람을 "제외한" 모든 방 멤버 ID
        val recipientUserIds = userRooms
            .map { it.user.id!! }
            .filter { it != senderId }

        // 알람 받을 사람이 1명 이상일 때만 전송
        if (recipientUserIds.isNotEmpty()) {
            val payload = event.payload

            val title = payload.sender.name ?: "새로운 메시지"

            val body = payload.content ?: when (payload.type) {
                MessageType.IMAGE -> "사진을 보냈습니다."
                MessageType.FILE -> "파일을 보냈습니다."
                MessageType.VIDEO -> "동영상을 보냈습니다."
                MessageType.AUDIO -> "음성을 보냈습니다."
                else -> "새 메시지"
            }

            val data = mapOf("roomId" to event.roomId)

            // 서비스 호출
            pushNotificationService.send(
                userIds = recipientUserIds,
                title = title,
                body = body,
                data = data
            )
        }
    }
}
