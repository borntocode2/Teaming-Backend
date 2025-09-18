package goodspace.teaming.assignment.domain.mapper

import goodspace.teaming.assignment.dto.SubmittedFileResponseDto
import goodspace.teaming.global.entity.file.File
import org.springframework.stereotype.Component

@Component
class SubmittedFileMapper {
    fun map(file: File): SubmittedFileResponseDto {
        return SubmittedFileResponseDto(
            fileId = file.id!!,
            fileName = file.name,
            fileType = file.type,
            mimeType = file.mimeType,
            fileSize = file.byteSize
        )
    }
}
