package goodspace.teaming.file.service

import goodspace.teaming.file.domain.S3PresignSupport
import goodspace.teaming.file.dto.DownloadUrlResponseDto
import goodspace.teaming.global.exception.FILE_NOT_FOUND
import goodspace.teaming.global.exception.NOT_MEMBER_OF_ROOM
import goodspace.teaming.global.exception.ORIGINAL_FILE_MISSING
import goodspace.teaming.global.repository.FileRepository
import goodspace.teaming.global.repository.UserRoomRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import software.amazon.awssdk.services.s3.model.S3Exception

@Service
class S3FileDownloadService(
    private val fileRepository: FileRepository,
    private val userRoomRepository: UserRoomRepository,
    private val s3: S3PresignSupport
) : FileDownloadService {

    @Transactional(readOnly = true)
    override fun issueDownloadUrl(userId: Long, fileId: Long): DownloadUrlResponseDto {
        val file = fileRepository.findById(fileId)
            .orElseThrow { IllegalArgumentException(FILE_NOT_FOUND) }

        require(userRoomRepository.existsByRoomIdAndUserId(file.room.id!!, userId)) { NOT_MEMBER_OF_ROOM }

        val head = try {
            s3.head(file.storageKey) // ← 변경: 일반 HEAD
        } catch (e: S3Exception) {
            throw IllegalArgumentException(ORIGINAL_FILE_MISSING, e)
        }

        val filename = file.name.ifBlank { file.storageKey.substringAfterLast('/') }
        val (url, expiresAt) = s3.presignGetWithDisposition(
            key = file.storageKey,
            filename = filename,
            contentType = head.contentType()
        )

        return DownloadUrlResponseDto(url = url, expiresAtEpochSeconds = expiresAt)
    }
}
