package goodspace.teaming.user.controller

import goodspace.teaming.global.security.getUserId
import goodspace.teaming.user.dto.UpdateEmailRequestDto
import goodspace.teaming.user.dto.UpdateNameRequestDto
import goodspace.teaming.user.dto.UpdatePasswordRequestDto
import goodspace.teaming.user.dto.UserInfoResponseDto
import goodspace.teaming.user.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

private val NO_CONTENT = ResponseEntity.noContent().build<Void>()

@RestController
@RequestMapping("/users/me")
@Tag(
    name = "회원 API",
    description = "회원 정보 조회 및 삭제 기능(아바타 수정은 파일 API에서 담당합니다)"
)
class UserController(
    private val userService: UserService
) {
    @GetMapping
    @Operation(
        summary = "회원 정보 조회"
    )
    fun getInfo(
        principal: Principal
    ): ResponseEntity<UserInfoResponseDto> {
        val userId = principal.getUserId()

        val response = userService.getUserInfo(userId)

        return ResponseEntity.ok(response)
    }

    @PatchMapping("/email")
    @Operation(
        summary = "이메일 수정",
        description = "이메일을 수정합니다. 새롭게 등록할 이메일이 인증되어 있어야 합니다."
    )
    fun updateEmail(
        principal: Principal,
        @RequestBody requestDto: UpdateEmailRequestDto
    ): ResponseEntity<Void> {
        val userId = principal.getUserId()

        userService.updateEmail(userId, requestDto)

        return NO_CONTENT
    }

    @PatchMapping("/password")
    @Operation(
        summary = "비밀번호 수정",
        description = "비밀번호를 수정합니다. 기존 비밀번호가 올바라야 합니다."
    )
    fun updatePassword(
        principal: Principal,
        @RequestBody requestDto: UpdatePasswordRequestDto
    ): ResponseEntity<Void> {
        val userId = principal.getUserId()

        userService.updatePassword(userId, requestDto)

        return NO_CONTENT
    }

    @PatchMapping("/name")
    @Operation(
        summary = "이름 수정",
    )
    fun updateName(
        principal: Principal,
        @RequestBody requestDto: UpdateNameRequestDto
    ): ResponseEntity<Void> {
        val userId = principal.getUserId()

        userService.updateName(userId, requestDto)

        return NO_CONTENT
    }
}
