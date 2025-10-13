package goodspace.teaming.file.service

import goodspace.teaming.file.domain.FileConstants
import goodspace.teaming.file.domain.FileValidation
import goodspace.teaming.file.domain.S3PresignSupport
import goodspace.teaming.file.dto.FileUploadCompleteRequestDto
import goodspace.teaming.file.dto.FileUploadCompleteResponseDto
import goodspace.teaming.file.dto.FileUploadIntentRequestDto
import goodspace.teaming.file.dto.FileUploadIntentResponseDto
import goodspace.teaming.file.event.FileUploadedEvent
import goodspace.teaming.global.entity.file.AntiVirusScanStatus
import goodspace.teaming.global.entity.file.File
import goodspace.teaming.global.entity.file.FileType
import goodspace.teaming.global.exception.INVALID_KEY_SCOPE
import goodspace.teaming.global.exception.INVALID_SIZE
import goodspace.teaming.global.exception.NOT_MEMBER_OF_ROOM
import goodspace.teaming.global.exception.ORIGINAL_FILE_MISSING
import goodspace.teaming.global.repository.FileRepository
import goodspace.teaming.global.repository.UserRoomRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import software.amazon.awssdk.services.s3.model.S3Exception
import java.net.URLConnection
import java.time.LocalDate
import java.util.UUID

@Service
class S3FileUploadService(
    private val validation: FileValidation,
    private val userRoomRepository: UserRoomRepository,
    private val fileRepository: FileRepository,
    private val s3: S3PresignSupport,
    private val eventPublisher: ApplicationEventPublisher,
    @Value("\${cloud.aws.uploads.prefix:chat}") private val prefix: String,
) : FileUploadService {
    @Transactional(readOnly = true)
    override fun intent(
        userId: Long,
        roomId: Long,
        requestDto: FileUploadIntentRequestDto
    ): FileUploadIntentResponseDto {
        validation.requireMembership(userId, roomId)
        validation.validateDeclaredSize(requestDto.size)

        val normalizedType = normalizeContentType(requestDto.contentType, requestDto.fileName)
        validation.validateAllowedContentType(normalizedType)

        val key = buildObjectKey(roomId, userId, requestDto.fileName)
        val url = s3.presignPut(key, normalizedType) // ← 체크섬 없이 발급

        return FileUploadIntentResponseDto(key = key, url = url)
    }

    @Transactional
    override fun complete(
        userId: Long,
        roomId: Long,
        requestDto: FileUploadCompleteRequestDto
    ): FileUploadCompleteResponseDto {
        val userRoom = userRoomRepository.findByRoomIdAndUserId(roomId, userId)
            ?: throw IllegalArgumentException(NOT_MEMBER_OF_ROOM)

        requireKeyBelongsToRoomAndUser(requestDto.key, roomId, userId)

        val head = try {
            s3.head(requestDto.key)
        } catch (e: S3Exception) {
            throw IllegalArgumentException(ORIGINAL_FILE_MISSING, e)
        }

        val sizeRange = FileConstants.MIN_OBJECT_BYTES..(FileConstants.MAX_UPLOAD_SIZE_MB * FileConstants.BYTES_PER_MB)
        if (head.contentLength() !in sizeRange) throw IllegalArgumentException(INVALID_SIZE)

        val mime = head.contentType() ?: guessContentTypeFromKey(requestDto.key)
        validation.validateAllowedContentType(mime)

        val storedName = requestDto.key.substringAfterLast('/')
        val file = fileRepository.save(
            File(
                room = userRoom.room,
                uploaderId = userRoom.user.id!!,
                name = extractOriginalName(storedName),
                type = mime.toFileType(),
                mimeType = mime,
                byteSize = head.contentLength(),
                storageKey = requestDto.key,
                storageBucket = s3.bucket(),
                antiVirusScanStatus = AntiVirusScanStatus.PASSED
            )
        )

        eventPublisher.publishEvent(
            FileUploadedEvent(
                fileId = file.id!!,
                bucket = s3.bucket(),
                key = file.storageKey,
                mimeType = file.mimeType
            )
        )

        return FileUploadCompleteResponseDto(fileId = file.id!!)
    }

    private fun buildObjectKey(roomId: Long, userId: Long, originalName: String): String {
        val today = LocalDate.now().format(FileConstants.DATE_FMT)
        val safe = originalName.replace(Regex("""[^A-Za-z0-9._-]"""), "_")
            .take(FileConstants.SAFE_NAME_MAX_LENGTH)
        val uuid = UUID.randomUUID()
        return "$prefix/$roomId/$userId/$today/${uuid}_$safe"
    }

    private fun normalizeContentType(contentType: String, fileName: String): String =
        contentType.ifBlank { URLConnection.guessContentTypeFromName(fileName) ?: "application/octet-stream" }

    private fun guessContentTypeFromKey(key: String): String =
        URLConnection.guessContentTypeFromName(key.substringAfterLast('/')) ?: "application/octet-stream"

    private fun requireKeyBelongsToRoomAndUser(key: String, roomId: Long, userId: Long) {
        require(key.startsWith("$prefix/$roomId/$userId/")) { INVALID_KEY_SCOPE }
    }

    private fun String.toFileType(): FileType = when {
        startsWith("image/") -> FileType.IMAGE
        startsWith("video/") -> FileType.VIDEO
        startsWith("audio/") -> FileType.AUDIO
        else -> FileType.FILE
    }

    private fun extractOriginalName(storedName: String): String {
        val idx = storedName.indexOf('_')
        return if (idx > 0 && storedName.take(idx).count { it == '-' } == 4 && storedName.take(idx).length == 36)
            storedName.substring(idx + 1) else storedName
    }
}
