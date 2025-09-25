package goodspace.teaming.assignment.domain.mapper

import goodspace.teaming.assignment.dto.AssignmentPreviewResponseDto
import goodspace.teaming.global.entity.aissgnment.Assignment
import org.springframework.stereotype.Component

@Component
class AssignmentPreviewMapper {
    fun map(assignment: Assignment): AssignmentPreviewResponseDto {
        return AssignmentPreviewResponseDto(
            assignmentId = assignment.id!!,
            roomId = assignment.room.id!!,
            title = assignment.title,
            description = assignment.description,
            due = assignment.due,
            status = assignment.status
        )
    }
}
