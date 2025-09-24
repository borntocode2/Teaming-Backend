package goodspace.teaming.authorization.service

import com.google.gson.Gson
import goodspace.teaming.authorization.dto.GoogleUserInfoResponseDto
import goodspace.teaming.authorization.dto.AppOauthRequestDto
import goodspace.teaming.global.entity.user.OAuthUser
import goodspace.teaming.global.entity.user.Role
import goodspace.teaming.global.entity.user.UserType
import goodspace.teaming.global.repository.UserRepository
import goodspace.teaming.global.security.TokenProvider
import goodspace.teaming.global.security.TokenResponseDto
import goodspace.teaming.global.security.TokenType
import org.apache.naming.ResourceRef.SCOPE
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import goodspace.teaming.authorization.dto.GoogleAccessTokenDto
import org.springframework.beans.factory.annotation.Value

@Service
class GoogleAuthService(
    private val restTemplate: RestTemplate,
    private val userRepository: UserRepository,
    private val toKenProvider: TokenProvider,

    @Value("\${keys.google.client-id-app}")
    private var googleClientId: String,
    @Value("\${keys.google.client-secret-app}")
    private var googleClientSecret: String,
    ) {

    @Transactional
    fun googleSignInOrSignUp(googleAccessTokenDto: GoogleAccessTokenDto): TokenResponseDto {
        val googleAccessToken = googleAccessTokenDto.accessToken
        val googleUserInfo: GoogleUserInfoResponseDto = getGoogleUserInfo(googleAccessToken)

        val user = userRepository.findByIdentifierAndUserType(
            identifier = googleUserInfo.id,
            userType = UserType.GOOGLE
        ) ?: run {
            val newUser = OAuthUser(
                identifier = googleUserInfo.id,
                email = googleUserInfo.email,
                name = googleUserInfo.name ?: "unknown",
//                thumbnailImageUrl = null,
//                profileImageUrl = googleUserInfo.pictureUrl,
                type = UserType.GOOGLE
            )
            userRepository.save(newUser)
        }

        val accessToken = toKenProvider.createToken(user.id!!, TokenType.ACCESS, listOf(Role.USER))
        val refreshToken = toKenProvider.createToken(user.id!!, TokenType.REFRESH, listOf(Role.USER))

        user.token = refreshToken
        userRepository.save(user)

        return TokenResponseDto(accessToken, refreshToken)
    }

    fun getAccessToken(appOauthRequestDto: AppOauthRequestDto): String {
        val params: Map<String, String> = getTokenParams(appOauthRequestDto.code, appOauthRequestDto.redirectUri)
        val response: ResponseEntity<String> = sendTokenRequest(params)

        if (isRequestFailed(response)) {
            throw IllegalStateException(
                "\"구글로부터 엑세스 토큰을 획득하지 못했습니다. 상태코드 {response.statusCode}: {response.body}\","
            )
        }

        return getAccessTokenFromResponse(response)
    }

    private fun getTokenParams(code: String, redirectUri: String): Map<String, String> {
        return java.util.Map.of<String, String>(
            "code", code,
            "client_id", googleClientId,
            "client_secret", googleClientSecret,
            "redirect_uri", redirectUri,
            "grant_type", "authorization_code"
        )
    }

    private fun sendTokenRequest(params: Map<String, String>): ResponseEntity<String> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        val TOKEN_BASE_URL: String = "https://oauth2.googleapis.com/token"

        val form = LinkedMultiValueMap<String, String>()

        form.setAll(params)
        val entity = HttpEntity<MultiValueMap<String, String>>(form, headers)

        return RestTemplate().postForEntity<String>(TOKEN_BASE_URL, entity, String::class.java)
    }

    private fun isRequestFailed(responseEntity: ResponseEntity<String>): Boolean {
        return !responseEntity.statusCode.is2xxSuccessful
    }

    private fun getAccessTokenFromResponse(responseEntity: ResponseEntity<String>): String {
        val json = responseEntity.body
            ?: throw IllegalStateException("응답 본문이 비어있습니다.")

        val tokenDto: GoogleAccessTokenDto? = Gson().fromJson(json, GoogleAccessTokenDto::class.java)
        return tokenDto?.accessToken
            ?: throw IllegalStateException("access_token이 응답에 없습니다.")
    }


    private fun getGoogleUserInfo(accessToken: String): GoogleUserInfoResponseDto {
        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $accessToken")
        }

        val entity = HttpEntity<Void>(headers)
        val response = restTemplate.exchange(
            "https://www.googleapis.com/oauth2/v2/userinfo",
            HttpMethod.GET,
            entity,
            GoogleUserInfoResponseDto::class.java
        )

        return response.body!! ?: throw IllegalArgumentException("구글 사용자 정보를 불러올 수 없습니다.")
    }
}

