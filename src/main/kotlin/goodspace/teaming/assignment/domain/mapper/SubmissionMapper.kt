package goodspace.teaming.assignment.domain.mapper

import goodspace.teaming.assignment.dto.SubmissionResponseDto
import goodspace.teaming.assignment.dto.SubmittedFileResponseDto
import goodspace.teaming.global.entity.aissgnment.Submission
import goodspace.teaming.global.entity.file.File
import org.springframework.stereotype.Component

@Component
class SubmissionMapper(
    private val submittedFileMapper: SubmittedFileMapper
) {
    fun map(submission: Submission): SubmissionResponseDto {
        return SubmissionResponseDto(
            submitterId = submission.submitterId,
            description = submission.description,
            createdAt = submission.createdAt!!,
            updatedAt = submission.updatedAt,
            files = submission.files.toDto()
        )
    }

    private fun List<File>.toDto(): List<SubmittedFileResponseDto> {
        return this.map { submittedFileMapper.map(it) }
    }
}
