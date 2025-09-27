package goodspace.teaming.chat.event

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class MemberEnteredEventHandler(
    private val messaging: SimpMessagingTemplate
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onReadBoundaryUpdated(event: MemberEnteredEvent) {
        messaging.convertAndSend(
            "/topic/rooms/${event.roomId}/enter",
            event.member
        )
    }
}
