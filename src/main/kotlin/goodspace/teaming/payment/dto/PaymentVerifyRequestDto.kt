package goodspace.teaming.payment.dto

class PaymentVerifyRequestDto (
    val amount: Long,
    val goodsName: String,
    val orderId: String
)