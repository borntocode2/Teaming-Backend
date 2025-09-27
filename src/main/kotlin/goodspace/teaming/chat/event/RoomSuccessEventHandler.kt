package goodspace.teaming.chat.event

import goodspace.teaming.chat.dto.RoomSuccessResponseDto
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class RoomSuccessEventHandler(
    private val messaging: SimpMessagingTemplate
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onReadBoundaryUpdated(event: RoomSuccessEvent) {
        messaging.convertAndSend(
            "/topic/rooms/${event.roomId}/success",
            RoomSuccessResponseDto(roomId = event.roomId)
        )
    }
}
