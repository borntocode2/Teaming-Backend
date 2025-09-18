package goodspace.teaming.assignment.dto

import java.time.Instant

data class AssignmentCreateRequestDto(
    val title: String,
    val description: String,
    val assignedMemberIds: List<Long>,
    val due: Instant
)
