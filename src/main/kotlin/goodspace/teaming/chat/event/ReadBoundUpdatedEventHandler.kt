package goodspace.teaming.chat.event

import goodspace.teaming.chat.dto.ReadBoundaryUpdateResponseDto
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ReadBoundUpdatedEventHandler(
    private val messaging: SimpMessagingTemplate
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onReadBoundaryUpdated(event: ReadBoundaryUpdatedEvent) {
        messaging.convertAndSend(
            "/topic/rooms/${event.roomId}/read",
            ReadBoundaryUpdateResponseDto(
                userId = event.userId,
                lastReadMessageId = event.lastReadMessageId,
                unreadCount = event.unreadCount
            )
        )
    }
}
