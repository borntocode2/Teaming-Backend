package goodspace.teaming.assignment.dto

import goodspace.teaming.global.entity.aissgnment.AssignmentStatus
import java.time.Instant

data class AssignmentResponseDto(
    val assignmentId: Long,
    val title: String,
    val description: String,
    val assignedMemberIds: List<Long>,
    val due: Instant,
    val status: AssignmentStatus,
    val submissions: List<SubmissionResponseDto>,
    val punished: Boolean
)
