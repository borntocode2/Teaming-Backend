package goodspace.teaming.assignment.dto

data class SubmissionRequestDto(
    val assignmentId: Long,
    val description: String,
    val fileIds: List<Long>
)
