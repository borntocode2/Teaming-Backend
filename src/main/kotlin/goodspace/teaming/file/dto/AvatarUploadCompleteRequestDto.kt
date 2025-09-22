package goodspace.teaming.file.dto

data class AvatarUploadCompleteRequestDto(
    val key: String,
    val width: Int? = null,
    val height: Int? = null
)
