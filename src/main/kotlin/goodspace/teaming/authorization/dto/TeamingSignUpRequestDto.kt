package goodspace.teaming.authorization.dto

data class TeamingSignUpRequestDto(
    val email: String,
    val password: String,
    val name: String,
)
