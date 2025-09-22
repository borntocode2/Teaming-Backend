package goodspace.teaming.file.dto

data class AvatarUploadIntentRequestDto(
    val contentType: String,
    val byteSize: Long,
    val checksumSha256Base64: String
)
