package goodspace.teaming.assignment.dto

import goodspace.teaming.global.entity.aissgnment.AssignmentStatus
import java.time.Instant

data class AssignmentPreviewResponseDto(
    val assignmentId: Long,
    val roomId: Long,
    val title: String,
    val description: String,
    val due: Instant,
    val status: AssignmentStatus
)
