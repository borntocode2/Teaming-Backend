package goodspace.teaming.payment.dto

data class CardInfoDto(
    val cardCode: String,
    val cardName: String,
    val cardNum: String,
    val cardQuota: Int,
    val isInterestFree: Boolean,
    val cardType: String,
    val canPartCancel: Boolean,
    val acquCardCode: String,
    val acquCardName: String
)