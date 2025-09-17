package goodspace.teaming.file.event

data class FileUploadedEvent(
    val fileId: Long,
    val bucket: String,
    val key: String,
    val mimeType: String
)
