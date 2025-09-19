package goodspace.teaming.assignment.controller

import goodspace.teaming.assignment.dto.SubmissionRequestDto
import goodspace.teaming.assignment.dto.AssignmentCreateRequestDto
import goodspace.teaming.assignment.dto.AssignmentResponseDto
import goodspace.teaming.assignment.service.AssignmentService
import goodspace.teaming.global.security.getUserId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

private val NO_CONTENT = ResponseEntity.noContent().build<Void>()

@RestController
@RequestMapping("/rooms/{roomId}/assignment")
@Tag(
    name = "과제 API"
)
class AssignmentController(
    private val assignmentService: AssignmentService
) {
    @GetMapping
    @Operation(
        summary = "과제 조회",
        description = "해당 방의 모든 과제를 조회합니다."
    )
    fun getAssignments(
        principal: Principal,
        @PathVariable roomId: Long
    ): ResponseEntity<List<AssignmentResponseDto>> {
        val userId = principal.getUserId()

        val response = assignmentService.get(userId, roomId)

        return ResponseEntity.ok(response)
    }

    @PostMapping
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

    @PostMapping("/submit")
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
}
