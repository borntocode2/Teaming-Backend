package goodspace.teaming.file.dto

data class FileUploadIntentRequestDto(
    val fileName: String,
    val contentType: String,
    val size: Long
)
