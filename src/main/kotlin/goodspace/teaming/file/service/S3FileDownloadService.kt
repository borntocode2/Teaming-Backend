package goodspace.teaming.file.service

import goodspace.teaming.file.domain.S3PresignSupport
import goodspace.teaming.file.dto.DownloadUrlResponseDto
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
    override fun issueDownloadUrl(
        userId: Long,
        fileId: Long
    ): DownloadUrlResponseDto {
        val file = fileRepository.findById(fileId)
            .orElseThrow { IllegalArgumentException("파일을 찾을 수 없습니다.") }

        require(userRoomRepository.existsByRoomIdAndUserId(file.room.id!!, userId)) { "해당 방 소속이 아닙니다." }

        val head = try {
            s3.headWithChecksum(file.storageKey)
        } catch (exception: S3Exception) {
            throw IllegalArgumentException("원본 파일이 존재하지 않습니다.", exception)
        }

        val filename = file.name.ifBlank { file.storageKey.substringAfterLast('/') }
        val (url, exp) = s3.presignGetWithDisposition(
            key = file.storageKey,
            filename = filename,
            contentType = head.contentType()
        )
        return DownloadUrlResponseDto(url = url, expiresAtEpochSeconds = exp)
    }
}
