package goodspace.teaming.file.service

import goodspace.teaming.file.dto.FileUploadCompleteRequestDto
import goodspace.teaming.file.dto.FileUploadCompleteResponseDto
import goodspace.teaming.file.dto.FileUploadIntentRequestDto
import goodspace.teaming.file.dto.FileUploadIntentResponseDto

interface FileUploadService {
    fun intent(
        userId: Long,
        roomId: Long,
        requestDto: FileUploadIntentRequestDto
    ): FileUploadIntentResponseDto

    fun complete(
        userId: Long,
        roomId: Long,
        requestDto: FileUploadCompleteRequestDto
    ): FileUploadCompleteResponseDto
}
