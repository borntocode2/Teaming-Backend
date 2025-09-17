package goodspace.teaming.file.dto

data class DownloadUrlResponseDto(
    val url: String,
    val expiresAtEpochSeconds: Long
)
