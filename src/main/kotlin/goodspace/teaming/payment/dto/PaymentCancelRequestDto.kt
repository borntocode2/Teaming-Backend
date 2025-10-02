package goodspace.teaming.payment.dto

import java.util.*

data class PaymentCancelRequestDto(
    val amount: String,
    val reason: String,
    val orderId: String = UUID.randomUUID().toString()
)
