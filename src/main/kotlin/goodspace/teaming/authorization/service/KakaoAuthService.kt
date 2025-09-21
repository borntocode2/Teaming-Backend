package goodspace.teaming.authorization.service

import com.google.gson.Gson
import goodspace.teaming.authorization.dto.GoogleAccessTokenDto
import goodspace.teaming.authorization.dto.KakaoAccessTokenDto
import goodspace.teaming.authorization.dto.KakaoUserInfoResponseDto
import goodspace.teaming.authorization.dto.WebOauthRequestDto
import goodspace.teaming.global.entity.user.OAuthUser
import goodspace.teaming.global.entity.user.Role
import goodspace.teaming.global.entity.user.UserType
import goodspace.teaming.global.repository.UserRepository
import goodspace.teaming.global.security.TokenProvider
import goodspace.teaming.global.security.TokenResponseDto
import goodspace.teaming.global.security.TokenType
import org.apache.naming.ResourceRef.SCOPE
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

@Service
class KakaoAuthService (
    private val restTemplate: RestTemplate,
    private val userRepository: UserRepository,
    private val toKenProvider: TokenProvider,

    @Value("\${keys.kakao.client-id}")
    private var kakaoClientId: String,

    @Value("\${keys.kakao.client-secret}")
    private var kakaoClientSecret: String

) {

    @Transactional
    fun kakaoSignInOrSignUp(kakaoAccessTokenDto: KakaoAccessTokenDto): TokenResponseDto {
        val kakaoAccessToken = kakaoAccessTokenDto.accessToken
            ?: throw IllegalArgumentException("No kakao access token")

        val kakaoUserInfo: KakaoUserInfoResponseDto = getKakaoUserInfo(kakaoAccessToken)

        val user = userRepository.findByIdentifierAndUserType(
            identifier = kakaoUserInfo.id.toString(),
            userType = UserType.KAKAO
        ) ?: run {
            val newUser = OAuthUser(
                identifier = kakaoUserInfo.id.toString(),
                email = kakaoUserInfo.kakaoAccount.email ?: "",
                name = kakaoUserInfo.kakaoAccount.name ?: "Unkown",
                thumbnailImageUrl = kakaoUserInfo.kakaoAccount.profile?.thumbnailImageUrl ?: "",
                profileImageUrl = kakaoUserInfo.kakaoAccount.profile?.profileImageUrl ?: "",
                type = UserType.KAKAO
            )
            userRepository.save(newUser)
        }

        val accessToken = toKenProvider.createToken(user.id!!, TokenType.ACCESS, listOf(Role.USER))
        val refreshToken = toKenProvider.createToken(user.id!!, TokenType.REFRESH, listOf(Role.USER))

        user.token = refreshToken
        userRepository.save(user)

        return TokenResponseDto(accessToken, refreshToken)
    }

    fun getAccessToken(webOauthRequestDto: WebOauthRequestDto): String {
        val params: Map<String, String> = getTokenParams(webOauthRequestDto.code, webOauthRequestDto.redirectUri)
        val response: ResponseEntity<String> = sendTokenRequest(params)

        if (isRequestFailed(response)) {
            throw IllegalStateException(
                "\"카카오로부터 엑세스 토큰을 획득하지 못했습니다. 상태코드 {response.statusCode}: {response.body}\","
            )
        }

        return getAccessTokenFromResponse(response)
    }

    private fun getTokenParams(code: String, redirectUri: String): Map<String, String> {
        return java.util.Map.of<String, String>(
            "code", code,
            "scope", SCOPE,
            "client_id", kakaoClientId,
            "client_secret", kakaoClientSecret,
            "redirect_uri", redirectUri,
            "grant_type", "authorization_code"
        )
    }

    private fun getAccessTokenFromResponse(responseEntity: ResponseEntity<String>): String {
        val json = responseEntity.body
            ?: throw IllegalStateException("응답 본문이 비어있습니다.")

        val tokenDto = Gson().fromJson(json, KakaoAccessTokenDto::class.java)
        return tokenDto.accessToken
            ?: throw IllegalStateException("access_token이 응답에 없습니다.")
    }

    private fun sendTokenRequest(params: Map<String, String>): ResponseEntity<String> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        val TOKEN_BASE_URL: String = "https://kauth.kakao.com/oauth/token"

        val form = LinkedMultiValueMap<String, String>()

        form.setAll(params)
        val entity = HttpEntity<MultiValueMap<String, String>>(form, headers)

        return RestTemplate().postForEntity<String>(TOKEN_BASE_URL, entity, String::class.java)
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

    private fun isRequestFailed(responseEntity: ResponseEntity<String>): Boolean {
        return !responseEntity.statusCode.is2xxSuccessful
    }
}