package goodspace.teaming.authorization.controller

import goodspace.teaming.authorization.dto.AppleSignInRequestDto
import goodspace.teaming.authorization.dto.GoogleAccessTokenDto
import goodspace.teaming.authorization.dto.KakaoAccessTokenDto
import goodspace.teaming.authorization.dto.NaverAccessTokenDto
import goodspace.teaming.authorization.service.AppleAuthService
import goodspace.teaming.authorization.service.GoogleAuthService
import goodspace.teaming.authorization.service.KakaoAuthService
import goodspace.teaming.authorization.service.NaverAuthService
import goodspace.teaming.global.security.TokenResponseDto
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth/web")
class WebAuthController(
    private val googleAuthService: GoogleAuthService,
    private val kakaoAuthService: KakaoAuthService,
    private val naverAuthService: NaverAuthService,
    private val appleAuthService: AppleAuthService
) {
    @PostMapping("/google")
    @Operation(summary = "구글 소셜 로그인(웹)", description = "구글이 발급한 AccessToken을 통해 사용자를 인증하고 JWT를 발급합니다")
    fun googleLogin(@RequestBody googleAccessTokenDto: GoogleAccessTokenDto): ResponseEntity<TokenResponseDto> {
        val tokenResponseDto = googleAuthService.googleSignInOrSignUp(googleAccessTokenDto)
        return ResponseEntity.ok(tokenResponseDto)
    }

    @PostMapping("/kakao")
    @Operation(summary = "카카오 소셜 로그인(웹)", description = "카카오가 발급한 AccessToken을 통해 사용자를 인증하고 JWT를 발급합니다")
    fun kakaoLogin(@RequestBody kakaoAccessTokenDto: KakaoAccessTokenDto): ResponseEntity<TokenResponseDto> {
        val tokenResponseDto = kakaoAuthService.kakaoSignInOrSignUp(kakaoAccessTokenDto)
        return ResponseEntity.ok(tokenResponseDto)
    }

    @PostMapping("/naver")
    @Operation(summary = "네이버 소셜 로그인(웹)", description = "네이버가 발급한 AccessToken을 통해 사용자를 인증하고 JWT를 발급합니다")
    fun kakaoLogin(@RequestBody naverAccessTokenDto: NaverAccessTokenDto): ResponseEntity<TokenResponseDto> {
        val tokenResponseDto = naverAuthService.NaverSignInOrSignUp(naverAccessTokenDto)
        return ResponseEntity.ok(tokenResponseDto)
    }

    @PostMapping("/apple")
    @Operation(
        summary = "애플 소셜 로그인(웹)",
        description = "애플이 발급한 AccessIdToken을 통해 사용자를 인증하고 JWT를 발급힙니다."
    )
    fun appleLogin(@RequestBody appleSignInRequestDto: AppleSignInRequestDto): ResponseEntity<TokenResponseDto> {
        val tokenResponseDto = appleAuthService.signInOrSignUp(appleSignInRequestDto)

        return ResponseEntity.ok(tokenResponseDto)
    }
}
