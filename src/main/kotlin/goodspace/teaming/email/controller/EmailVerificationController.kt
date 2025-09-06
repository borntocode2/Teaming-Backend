package goodspace.teaming.email.controller

import goodspace.teaming.email.dto.CodeSendRequestDto
import goodspace.teaming.email.dto.EmailVerifyRequestDto
import goodspace.teaming.email.service.EmailVerificationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val NO_CONTENT: ResponseEntity<Void> = ResponseEntity.noContent().build()

@RestController
@RequestMapping("/email")
@Tag(
    name = "이메일 API",
    description = "이메일 인증 관련"
)
class EmailVerificationController(
    private val emailVerificationService: EmailVerificationService
) {
    @PostMapping("/send-code")
    @Operation(
        summary = "인증 코드 전송",
        description = "특정 메일로 인증 코드를 전송합니다. 해당 이메일로 이미 회원가입되어 있어야 할 때는 shouldAlreadyExists 값을 true로, 아니라면 false로 지정해야 합니다."
    )
    fun sendCode(
        @RequestBody requestDto: CodeSendRequestDto
    ): ResponseEntity<Void> {
        emailVerificationService.publishVerificationCode(requestDto)

        return NO_CONTENT
    }

    @PostMapping("/verify-code")
    @Operation(
        summary = "인증 코드 확인",
        description = "인증 코드가 일치하는지 확인합니다."
    )
    fun verifyCode(
        @RequestBody requestDto: EmailVerifyRequestDto
    ): ResponseEntity<Void> {
        emailVerificationService.verifyEmail(requestDto)

        return NO_CONTENT
    }
}
