package goodspace.teaming.assignment.dto

import goodspace.teaming.global.entity.file.FileType

data class SubmittedFileResponseDto(
    val fileId: Long,
    val fileName: String,
    val fileType: FileType,
    val mimeType: String,
    val fileSize: Long,
)
