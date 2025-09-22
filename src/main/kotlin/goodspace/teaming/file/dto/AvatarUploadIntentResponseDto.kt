package goodspace.teaming.file.dto

data class AvatarUploadIntentResponseDto(
    val key: String,
    val bucket: String,
    val url: String,
    val requiredHeaders: Map<String, String>
)
