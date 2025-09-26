package goodspace.teaming.user.dto

data class UserInfoResponseDto(
    val email: String,
    val name: String,
    val avatarUrl: String?,
    val avatarVersion: Int
)
