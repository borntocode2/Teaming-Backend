package goodspace.teaming.chat.event

import goodspace.teaming.chat.dto.RoomLeaveResponseDto
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class RoomLeaveEventHandler(
    private val messaging: SimpMessagingTemplate
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onReadBoundaryUpdated(event: RoomLeaveEvent) {
        messaging.convertAndSend(
            "/topic/rooms/${event.roomId}/leave",
            RoomLeaveResponseDto(
                memberId = event.memberId,
                roomId = event.roomId
            )
        )
    }
}
