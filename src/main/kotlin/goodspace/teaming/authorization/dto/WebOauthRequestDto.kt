package goodspace.teaming.authorization.dto

data class WebOauthRequestDto(
    val code: String,
    val redirectUri: String
)
