package goodspace.teaming.assignment.controller

import goodspace.teaming.assignment.dto.SubmissionRequestDto
import goodspace.teaming.assignment.dto.AssignmentCreateRequestDto
import goodspace.teaming.assignment.dto.AssignmentPreviewResponseDto
import goodspace.teaming.assignment.dto.AssignmentResponseDto
import goodspace.teaming.assignment.service.AssignmentService
import goodspace.teaming.global.security.getUserId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

private val NO_CONTENT = ResponseEntity.noContent().build<Void>()

@RestController
@RequestMapping("/rooms")
@Tag(
    name = "과제 API"
)
class AssignmentController(
    private val assignmentService: AssignmentService
) {
    @GetMapping("/assignments")
    @Operation(
        summary = "할당된 과제 조회",
        description = "해당 사용자에게 할당된 과제 중, 아직 마감되지 않은 과제를 모두 반환합니다."
    )
    fun getEveryAssignedAssignment(
        principal: Principal
    ): ResponseEntity<List<AssignmentPreviewResponseDto>> {
        val userId = principal.getUserId()

        val response = assignmentService.getAssignedAssignments(userId)

        return ResponseEntity.ok(response)
    }

    @GetMapping("/{roomId}/assignments")
    @Operation(
        summary = "티밍룸 과제 조회",
        description = "해당 티밍룸에 존재하는 모든 과제를 조회합니다."
    )
    fun getAssignmentsInRoom(
        principal: Principal,
        @PathVariable roomId: Long
    ): ResponseEntity<List<AssignmentResponseDto>> {
        val userId = principal.getUserId()

        val response = assignmentService.getAssignmentsInRoom(userId, roomId)

        return ResponseEntity.ok(response)
    }

    @PostMapping("/{roomId}/assignments")
    @Operation(
        summary = "과제 생성",
        description = "새로운 과제를 생성합니다."
    )
    fun createAssignment(
        principal: Principal,
        @PathVariable roomId: Long,
        @RequestBody requestDto: AssignmentCreateRequestDto
    ): ResponseEntity<Void> {
        val userId = principal.getUserId()

        assignmentService.create(userId, roomId, requestDto)

        return NO_CONTENT
    }

    @PostMapping("/{roomId}/assignments/submit")
    @Operation(
        summary = "과제 제출",
        description = "과제를 제출합니다. 과제가 완수됩니다."
    )
    fun completeAssignment(
        principal: Principal,
        @PathVariable roomId: Long,
        @RequestBody requestDto: SubmissionRequestDto
    ): ResponseEntity<Void> {
        val userId = principal.getUserId()

        assignmentService.submit(userId, roomId, requestDto)

        return NO_CONTENT
    }

    @DeleteMapping("/{roomId}/assignments/{assignmentId}")
    @Operation(
        summary = "과제 취소",
        description = "과제를 취소합니다. 아직 완료하지 않은 과제에 한해서만 호출 가능합니다. 팀장만 호출할 수 있습니다."
    )
    fun cancelAssignment(
        principal: Principal,
        @PathVariable roomId: Long,
        @PathVariable assignmentId: Long
    ): ResponseEntity<Void> {
        val userId = principal.getUserId()

        assignmentService.cancel(userId, roomId, assignmentId)

        return NO_CONTENT
    }
}
