package goodspace.teaming.file.service

import goodspace.teaming.file.dto.DownloadUrlResponseDto

interface FileDownloadService {
    fun issueDownloadUrl(
        userId: Long,
        fileId: Long
    ): DownloadUrlResponseDto
}
