package goodspace.teaming.authorization.controller

import goodspace.teaming.authorization.dto.TeamingSignInRequestDto
import goodspace.teaming.authorization.dto.TeamingSignUpRequestDto
import goodspace.teaming.authorization.service.TeamingAuthService
import goodspace.teaming.global.security.TokenResponseDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth/teaming")
@Tag(
    name = "티밍 회원가입/로그인 API"
)
class TeamingAuthController(
    private val teamingAuthService: TeamingAuthService
) {
    @PostMapping("/sign-up")
    @Operation(
        summary = "회원 가입",
        description = "새로운 회원을 생성합니다. 사전에 이메일이 인증되어 있어야 합니다. 기존 리프레쉬 토큰을 만료시킵니다."
    )
    fun signUp(
        @RequestBody requestDto: TeamingSignUpRequestDto,
        @RequestParam(required = false) isMobile: Boolean = false
    ): ResponseEntity<TokenResponseDto> {
        val response = teamingAuthService.signUp(requestDto, isMobile)

        return ResponseEntity.ok(response)
    }

    @PostMapping("/sign-in")
    @Operation(
        summary = "로그인",
        description = "이메일과 비밀번호를 통해 JWT를 발급합니다. 기존 리프레쉬 토큰을 만료시킵니다."
    )
    fun signIn(
        @RequestBody requestDto: TeamingSignInRequestDto,
        @RequestParam(required = false) isMobile: Boolean = false
    ): ResponseEntity<TokenResponseDto> {
        val response = teamingAuthService.signIn(requestDto, isMobile)

        return ResponseEntity.ok(response)
    }
}
