package goodspace.teaming.push.dto

data class ExpoPushTicketDto(
    val status: String, // "ok" or "error"
    val id: String? = null,
    val message: String? = null,
    val details: ExpoErrorDetailsDto? = null
)
