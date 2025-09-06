package goodspace.teaming.email.dto

data class CodeSendRequestDto(
    val email: String,
    val shouldAlreadyExists: Boolean
)
