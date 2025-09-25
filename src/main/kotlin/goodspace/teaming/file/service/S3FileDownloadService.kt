package goodspace.teaming.file.service

import goodspace.teaming.file.domain.S3PresignSupport
import goodspace.teaming.file.dto.DownloadUrlResponseDto
import goodspace.teaming.global.repository.FileRepository
import goodspace.teaming.global.repository.UserRoomRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import software.amazon.awssdk.services.s3.model.S3Exception

private const val MSG_FILE_NOT_FOUND = "파일을 찾을 수 없습니다."
private const val MSG_NOT_ROOM_MEMBER = "해당 방 소속이 아닙니다."
private const val MSG_ORIGINAL_MISSING = "원본 파일이 존재하지 않습니다."

@Service
class S3FileDownloadService(
    private val fileRepository: FileRepository,
    private val userRoomRepository: UserRoomRepository,
    private val s3: S3PresignSupport
) : FileDownloadService {

    @Transactional(readOnly = true)
    override fun issueDownloadUrl(userId: Long, fileId: Long): DownloadUrlResponseDto {
        val file = fileRepository.findById(fileId)
            .orElseThrow { IllegalArgumentException(MSG_FILE_NOT_FOUND) }

        require(userRoomRepository.existsByRoomIdAndUserId(file.room.id!!, userId)) { MSG_NOT_ROOM_MEMBER }

        val head = try {
            s3.head(file.storageKey) // ← 변경: 일반 HEAD
        } catch (e: S3Exception) {
            throw IllegalArgumentException(MSG_ORIGINAL_MISSING, e)
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
