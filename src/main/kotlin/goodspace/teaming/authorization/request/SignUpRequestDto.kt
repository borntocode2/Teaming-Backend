package goodspace.teaming.authorization.request

data class SignUpRequestDto(
    val email: String,
    val password: String,
    val nickname: String,
    val avatarKey: String? = null,
    val avatarVersion: Int? = null
)
