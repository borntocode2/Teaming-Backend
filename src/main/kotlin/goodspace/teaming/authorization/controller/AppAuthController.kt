package goodspace.teaming.authorization.controller

import goodspace.teaming.authorization.dto.GoogleAccessTokenDto
import goodspace.teaming.authorization.dto.KakaoAccessTokenDto
import goodspace.teaming.authorization.dto.NaverAccessTokenDto
import goodspace.teaming.authorization.dto.AppOauthRequestDto
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
@RequestMapping("/api/auth/app")
class AppAuthController (
    private val googleAuthService: GoogleAuthService,
    private val kakaoAuthService: KakaoAuthService,
    private val naverAuthService: NaverAuthService
)
{
    @PostMapping("/google")
    @Operation(summary = "구글 소셜 로그인(앱)",
        description = "구글을 통해 사용자를 인증하고 JWT를 발급합니다")

    fun googleAppAuthorization(@RequestBody requestDto: AppOauthRequestDto): ResponseEntity<TokenResponseDto?>? {
        val accessToken: String= googleAuthService.getAccessToken(requestDto)

        val tokenResponseDto: TokenResponseDto =
            googleAuthService.googleSignInOrSignUp(GoogleAccessTokenDto(accessToken = accessToken))

        return ResponseEntity.ok<TokenResponseDto>(tokenResponseDto)
    }

    @PostMapping("/kakao")
    @Operation(summary = "카카오 소셜 로그인(앱)",
        description = "카카오을 통해 사용자를 인증하고 JWT를 발급합니다")


    fun kakaoAppAuthorization(@RequestBody requestDto: AppOauthRequestDto): ResponseEntity<TokenResponseDto?>? {
        val accessToken: String= kakaoAuthService.getAccessToken(requestDto)

        val tokenResponseDto: TokenResponseDto =
            kakaoAuthService.kakaoSignInOrSignUp(KakaoAccessTokenDto(accessToken = accessToken))

        return ResponseEntity.ok<TokenResponseDto>(tokenResponseDto)
    }

    @PostMapping("/naver")
    @Operation(summary = "네이버 소셜 로그인(앱)",
        description = "네이버을 통해 사용자를 인증하고 JWT를 발급합니다")


    fun naverAppAuthorization(@RequestBody requestDto: AppOauthRequestDto): ResponseEntity<TokenResponseDto?>? {
        val accessToken: String= naverAuthService.getAccessToken(requestDto)

        val tokenResponseDto: TokenResponseDto =
            naverAuthService.NaverSignInOrSignUp(NaverAccessTokenDto(accessToken = accessToken))

        return ResponseEntity.ok<TokenResponseDto>(tokenResponseDto)
    }
}
