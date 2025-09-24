package goodspace.teaming.authorization.dto

data class AppleOauthRequestDto(
    val code: String,
    val redirectUri: String,
    val name: String? = null,
    val codeVerifier: String
)
