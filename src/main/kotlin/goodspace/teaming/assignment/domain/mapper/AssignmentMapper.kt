package goodspace.teaming.assignment.domain.mapper

import goodspace.teaming.assignment.dto.AssignmentCreateRequestDto
import goodspace.teaming.assignment.dto.AssignmentResponseDto
import goodspace.teaming.assignment.dto.SubmissionResponseDto
import goodspace.teaming.global.entity.aissgnment.Assignment
import goodspace.teaming.global.entity.aissgnment.AssignmentStatus.IN_PROGRESS
import goodspace.teaming.global.entity.aissgnment.Submission
import goodspace.teaming.global.entity.room.Room
import org.springframework.stereotype.Component

@Component
class AssignmentMapper(
    private val submissionMapper: SubmissionMapper
) {
    fun map(room: Room, dto: AssignmentCreateRequestDto): Assignment {
        return Assignment(
            title = dto.title,
            description = dto.description,
            room = room,
            due = dto.due,
            status = IN_PROGRESS
        )
    }

    fun map(assignment: Assignment): AssignmentResponseDto {
        return AssignmentResponseDto(
            assignmentId = assignment.id!!,
            title = assignment.title,
            description = assignment.description,
            assignedMemberIds = assignment.assignedMemberIds,
            due = assignment.due,
            status = assignment.status,
            submissions = assignment.submissions.toDto(),
            punished = assignment.punished
        )
    }

    private fun List<Submission>.toDto(): List<SubmissionResponseDto> {
        return this.map { submissionMapper.map(it) }
    }
}
