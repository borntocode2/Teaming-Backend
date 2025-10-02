package goodspace.teaming.payment.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class PaymentCancelResponseDto(
    @JsonProperty("resultCode")
    val resultCode: String,

    @JsonProperty("resultMsg")
    val resultMsg: String,

    @JsonProperty("tid")
    val tid: String,

    @JsonProperty("cancelledTid")
    val cancelledTid: String?,

    @JsonProperty("orderId")
    val orderId: String,

    @JsonProperty("ediDate")
    val ediDate: String?,

    @JsonProperty("signature")
    val signature: String?,

    @JsonProperty("status")
    val status: String,

    @JsonProperty("paidAt")
    val paidAt: String?,

    @JsonProperty("failedAt")
    val failedAt: String?,

    @JsonProperty("cancelledAt")
    val cancelledAt: String?,

    @JsonProperty("payMethod")
    val payMethod: String,

    @JsonProperty("amount")
    val amount: Int,

    @JsonProperty("balanceAmt")
    val balanceAmount: Int,

    @JsonProperty("goodsName")
    val goodsName: String,

    @JsonProperty("mallReserved")
    val mallReserved: String?,

    @JsonProperty("useEscrow")
    val useEscrow: Boolean,

    @JsonProperty("currency")
    val currency: String,

    @JsonProperty("channel")
    val channel: String?,

    @JsonProperty("approveNo")
    val approveNo: String,

    @JsonProperty("buyerName")
    val buyerName: String?,

    @JsonProperty("buyerTel")
    val buyerTel: String?,

    @JsonProperty("buyerEmail")
    val buyerEmail: String?,

    @JsonProperty("issuedCashReceipt")
    val issuedCashReceipt: Boolean,

    @JsonProperty("receiptUrl")
    val receiptUrl: String,

    @JsonProperty("mallUserId")
    val mallUserId: String?
)