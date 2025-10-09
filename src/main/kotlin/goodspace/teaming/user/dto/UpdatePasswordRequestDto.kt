package goodspace.teaming.user.dto

data class UpdatePasswordRequestDto(
    val currentPassword: String,
    val newPassword: String,
    val email: String
)
