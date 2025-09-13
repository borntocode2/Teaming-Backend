package goodspace.teaming.chat.dto

data class SenderSummaryResponseDto(
    val id: Long?,
    val name: String,
    val avatarUrl: String? = null
)
