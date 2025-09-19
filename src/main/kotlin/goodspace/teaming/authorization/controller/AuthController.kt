package goodspace.teaming.authorization.controller

import goodspace.teaming.authorization.dto.KakaoAccessTokenDto
import goodspace.teaming.authorization.dto.NaverAccessTokenDto
import goodspace.teaming.authorization.dto.OauthAccessTokenDto
import goodspace.teaming.authorization.service.GoogleAuthService
import goodspace.teaming.authorization.service.KakaoAuthService
import goodspace.teaming.authorization.service.NaverAuthService
import goodspace.teaming.global.security.TokenResponse
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val googleAuthService: GoogleAuthService,
    private val kakaoAuthService: KakaoAuthService,
    private val naverAuthService: NaverAuthService
) {
    @PostMapping("/google")
    @Operation(summary = "구글 소셜 로그인(앱)", description = "구글가 발급한 AccessToken을 통해 사용자를 인증하고 JWT를 발급합니다")
    fun googleLogin(@RequestBody googleAccessTokenDto: OauthAccessTokenDto): ResponseEntity<TokenResponse> {
        val tokenResponse = googleAuthService.googleSignInOrSignUp(googleAccessTokenDto)
        return ResponseEntity.ok(tokenResponse)
    }

    @PostMapping("/kakao")
    @Operation(summary = "카카오 소셜 로그인(앱)", description = "카카오가 발급한 AccessToken을 통해 사용자를 인증하고 JWT를 발급합니다")
    fun kakaoLogin(@RequestBody kakaoAccessTokenDto: KakaoAccessTokenDto): ResponseEntity<TokenResponse> {
        val tokenResponse = kakaoAuthService.kakaoSignInOrSignUp(kakaoAccessTokenDto)
        return ResponseEntity.ok(tokenResponse)
    }

    @PostMapping("/naver")
    @Operation(summary = "네이버 소셜 로그인(앱)", description = "네이버가 발급한 AccessToken을 통해 사용자를 인증하고 JWT를 발급합니다")
    fun kakaoLogin(@RequestBody naverAccessTokenDto: NaverAccessTokenDto): ResponseEntity<TokenResponse> {
        val tokenResponse = naverAuthService.NaverSignInOrSignUp(naverAccessTokenDto)
        return ResponseEntity.ok(tokenResponse)
    }
}