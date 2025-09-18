package goodspace.teaming.assignment.service

import goodspace.teaming.assignment.dto.SubmissionRequestDto
import goodspace.teaming.assignment.dto.AssignmentCreateRequestDto
import goodspace.teaming.assignment.dto.AssignmentResponseDto

interface AssignmentService {
    fun create(
        userId: Long,
        roomId: Long,
        requestDto: AssignmentCreateRequestDto
    )

    fun get(
        userId: Long,
        roomId: Long
    ): List<AssignmentResponseDto>

    fun submit(
        userId: Long,
        roomId: Long,
        requestDto: SubmissionRequestDto
    )
}
