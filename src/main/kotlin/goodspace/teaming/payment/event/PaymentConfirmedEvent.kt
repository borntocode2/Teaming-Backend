package goodspace.teaming.payment.event

data class PaymentConfirmedEvent(
    val userId: Long,
    val roomId: Long
)
