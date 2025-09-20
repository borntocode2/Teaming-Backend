package goodspace.teaming.authorization.service

import goodspace.teaming.authorization.dto.KakaoAccessTokenDto
import goodspace.teaming.authorization.dto.OauthAccessTokenDto
import goodspace.teaming.authorization.dto.KakaoUserInfoResponseDto
import goodspace.teaming.global.entity.user.OAuthUser
import goodspace.teaming.global.entity.user.Role
import goodspace.teaming.global.entity.user.UserType
import goodspace.teaming.global.repository.UserRepository
import goodspace.teaming.global.security.TokenProvider
import goodspace.teaming.global.security.TokenResponse
import goodspace.teaming.global.security.TokenType
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class KakaoAuthService (
    private val restTemplate: RestTemplate,
    private val userRepository: UserRepository,
    private val toKenProvider: TokenProvider
) {
    fun kakaoSignInOrSignUp(kakaoAccessTokenDto: KakaoAccessTokenDto): TokenResponse {
        val kakaoUserInfo: KakaoUserInfoResponseDto = getKakaoUserInfo(kakaoAccessTokenDto.accessToken)

        val user = userRepository.findByIdentifierAndUserType(
            identifier = kakaoUserInfo.id,
            userType = UserType.KAKAO
        ) ?: run {
            val newUser = OAuthUser(
                identifier = kakaoUserInfo.id,
                email = kakaoUserInfo.email,
                name = kakaoUserInfo.name,
                type = UserType.KAKAO
            )
            userRepository.save(newUser)
        }

        val accessToken = toKenProvider.createToken(user.id!!, TokenType.ACCESS, listOf(Role.USER))
        val refreshToken = toKenProvider.createToken(user.id!!, TokenType.REFRESH, listOf(Role.USER))

        user.token = refreshToken
        userRepository.save(user)

        return TokenResponse(accessToken, refreshToken)
    }

    private fun getKakaoUserInfo(accessToken: String): KakaoUserInfoResponseDto {
        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $accessToken")
        }

        val entity = HttpEntity<Void>(headers)
        val response = restTemplate.exchange(
            "https://kapi.kakao.com/v2/user/me",
            HttpMethod.GET,
            entity,
            KakaoUserInfoResponseDto::class.java
        )

        return response.body!! ?: throw IllegalArgumentException("카카오 사용자 정보를 불러올 수 없습니다.")
    }
}