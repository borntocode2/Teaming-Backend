package goodspace.teaming.chat.dto

import goodspace.teaming.global.entity.file.AntiVirusScanStatus
import goodspace.teaming.global.entity.file.FileType
import goodspace.teaming.global.entity.file.TranscodeStatus

data class MessageAttachmentResponseDto(
    val fileId: Long,
    val uploaderId: Long,
    val sortOrder: Int,
    val name: String,
    val type: FileType,
    val mimeType: String,
    val byteSize: Long,
    val width: Int? = null,
    val height: Int? = null,
    val durationMs: Int? = null,
    val previewUrl: String? = null,
    val thumbnailUrl: String? = null,
    val downloadUrl: String? = null,
    val antiVirusScanStatus: AntiVirusScanStatus,
    val transcodeStatus: TranscodeStatus,
    val ready: Boolean
)
