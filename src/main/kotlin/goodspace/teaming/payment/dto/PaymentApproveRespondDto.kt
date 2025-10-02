package goodspace.teaming.payment.dto

import goodspace.teaming.payment.domain.CardInfo
import goodspace.teaming.payment.domain.PaymentApproveRespond
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class PaymentApproveRespondDto(
    val resultCode: String,
    val resultMsg: String,
    val tid: String,
    val cancelledTid: String?,
    val orderId: String,
    val ediDate: String?,
    val signature: String,
    val status: String,
    val paidAt: String?,
    val failedAt: String?,
    val cancelledAt: String?,
    val payMethod: String,
    val amount: Int,
    val balanceAmt: Int,
    val goodsName: String,
    val mallReserved: String?,
    val useEscrow: Boolean,
    val currency: String,
    val channel: String,
    val approveNo: String,
    val buyerName: String?,
    val buyerTel: String?,
    val buyerEmail: String?,
    val receiptUrl: String,
    val mallUserId: String?,
    val issuedCashReceipt: Boolean,
    val card: CardInfoDto?
) {

    fun toEntity(): PaymentApproveRespond {
        val cardInfo = this.card?.let { cardDto ->
            CardInfo(
                cardCode = cardDto.cardCode,
                cardName = cardDto.cardName,
                cardNum = cardDto.cardNum,
                cardQuota = cardDto.cardQuota,
                isInterestFree = cardDto.isInterestFree,
                cardType = cardDto.cardType,
                canPartCancel = cardDto.canPartCancel,
                acquCardCode = cardDto.acquCardCode,
                acquCardName = cardDto.acquCardName
            )
        }

        return PaymentApproveRespond(
            resultCode = resultCode,
            resultMsg = resultMsg,
            tid = tid,
            cancelledTid = cancelledTid,
            orderId = orderId,
            ediDate = parseToLocalDateTime(ediDate),
            signature = signature,
            status = status,
            paidAt = parseToLocalDateTime(paidAt),
            failedAt = parseToLocalDateTimeOrNull(failedAt),
            cancelledAt = parseToLocalDateTimeOrNull(cancelledAt),
            payMethod = payMethod,
            amount = amount,
            balanceAmt = balanceAmt,
            goodsName = goodsName,
            mallReserved = mallReserved,
            useEscrow = useEscrow,
            currency = currency,
            channel = channel,
            approveNo = approveNo,
            buyerName = buyerName,
            buyerTel = buyerTel,
            buyerEmail = buyerEmail?.takeIf { it != "null" },
            receiptUrl = receiptUrl,
            mallUserId = mallUserId,
            issuedCashReceipt = issuedCashReceipt,
            user = null,
            room = null
        ).apply {
            this.card = cardInfo
        }
    }

    private fun parseToLocalDateTime(value: String?): LocalDateTime? {
        if (value == null) return null
        val v = value.trim()
        if (v == "0" || v.equals("null", true) || v.isEmpty()) return null

        val patterns = listOf(
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"),
            DateTimeFormatter.ISO_OFFSET_DATE_TIME
        )

        for (fmt in patterns) {
            try {
                val odt = OffsetDateTime.parse(v, fmt)
                return odt.toLocalDateTime()
            } catch (_: DateTimeParseException) {
            }
        }
        return null
    }

    private fun parseToLocalDateTimeOrNull(value: String?): LocalDateTime? {
        return parseToLocalDateTime(value)
    }
}