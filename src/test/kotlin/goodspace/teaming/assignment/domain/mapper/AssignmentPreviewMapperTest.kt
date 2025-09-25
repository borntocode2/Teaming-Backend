package goodspace.teaming.assignment.domain.mapper

import goodspace.teaming.util.createAssignment
import goodspace.teaming.util.createRoom
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AssignmentPreviewMapperTest {
    private val assignmentPreviewMapper = AssignmentPreviewMapper()

    @Test
    fun `Assignment의 속성을 기반으로 DTO를 생성한다`() {
        // given
        val room = createRoom()
        val assignment = createAssignment(room = room)

        // when
        val result = assignmentPreviewMapper.map(assignment)

        // then
        assertThat(result.assignmentId).isEqualTo(assignment.id)
        assertThat(result.roomId).isEqualTo(room.id)
        assertThat(result.title).isEqualTo(assignment.title)
        assertThat(result.description).isEqualTo(assignment.description)
        assertThat(result.due).isEqualTo(assignment.due)
        assertThat(result.status).isEqualTo(assignment.status)
    }
}
