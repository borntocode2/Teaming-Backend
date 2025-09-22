package goodspace.teaming.user.dto

data class UserInfoResponseDto(
    val email: String,
    val name: String,
    val avatarKey: String?,
    val avatarVersion: Int?
)
