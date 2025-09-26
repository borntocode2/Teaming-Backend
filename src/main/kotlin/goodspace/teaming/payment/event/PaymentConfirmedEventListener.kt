package goodspace.teaming.payment.event

import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class PaymentConfirmedEventListener {
    @EventListener(PaymentConfirmedEvent::class)
    @Async
    fun onPaymentConfirmed(event: PaymentConfirmedEvent) {
        // DB whghl
    }
}