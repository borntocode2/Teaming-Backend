package goodspace.teaming.payment.dto

class PaymentVerifyRespondDto (
    val authResultCode: String,
    val authResultMsg: String,
    val tid: String,
    val clientId :String,
    val orderId: String,
    val amount: String,
    val mallReserved: String,
    val authToken: String,
    val signature: String
){
    fun PaymentVerifyRespondDto.toApproveRequestDto(): PaymentApproveRequestDto{
        return PaymentApproveRequestDto(
            tid = this.tid,
            amount = this.amount
        )
    }
}