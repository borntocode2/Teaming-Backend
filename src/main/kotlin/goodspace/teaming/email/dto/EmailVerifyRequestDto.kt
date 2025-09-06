package goodspace.teaming.email.dto

data class EmailVerifyRequestDto(
    val email: String,
    val code: String
)
