package goodspace.teaming.authorization.dto

data class AppleSignInRequestDto(
    val accessIdToken: String,
    val name: String? = null
)
