package goodspace.teaming.file.dto

data class AvatarUploadCompleteResponseDto(
    val avatarKey: String,
    val avatarVersion: Int,
    val publicUrl: String
)
