package goodspace.teaming.assignment.dto

import java.time.Instant

data class SubmissionResponseDto(
    val submitterId: Long,
    val description: String,
    val createdAt: Instant,
    val updatedAt: Instant?,
    val files: List<SubmittedFileResponseDto>
)
