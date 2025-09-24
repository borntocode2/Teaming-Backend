package goodspace.teaming.chat.domain.mapper

import goodspace.teaming.chat.dto.MessageAttachmentResponseDto
import goodspace.teaming.global.entity.file.AntiVirusScanStatus
import goodspace.teaming.global.entity.file.Attachment
import goodspace.teaming.global.entity.file.FileType
import goodspace.teaming.global.entity.file.TranscodeStatus
import goodspace.teaming.file.domain.StorageUrlProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AttachmentMapper(
    private val urlProvider: StorageUrlProvider,
    @Value("\${mapper.attachment.thumbnail-size:256}")
    private val thumbnailSize: Int = 256
) {
    fun map(attachment: Attachment): MessageAttachmentResponseDto {
        val file = attachment.file
        val avPassed = file.antiVirusScanStatus == AntiVirusScanStatus.PASSED
        val transcodeOk = when (file.type) {
            FileType.VIDEO, FileType.AUDIO -> file.transcodeStatus == TranscodeStatus.COMPLETED
            else -> true
        }

        return MessageAttachmentResponseDto(
            fileId = requireNotNull(file.id),
            uploaderId = file.uploaderId,
            sortOrder = attachment.sortOrder,
            name = file.name,
            type = file.type,
            mimeType = file.mimeType,
            byteSize = file.byteSize,
            width = file.width, height = file.height, durationMs = file.durationMs,
            previewUrl = urlProvider.publicUrl(file.storageKey),
            thumbnailUrl = file.thumbnailKey?.let { key ->
                urlProvider.publicUrl(key = key, size = thumbnailSize)
            },
            downloadUrl = urlProvider.downloadUrl(key = file.storageKey, filename =  file.name),
            antiVirusScanStatus = file.antiVirusScanStatus,
            transcodeStatus = file.transcodeStatus,
            ready = avPassed && transcodeOk
        )
    }
}
