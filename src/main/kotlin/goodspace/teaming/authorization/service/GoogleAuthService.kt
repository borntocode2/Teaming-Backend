package goodspace.teaming.authorization.service

import goodspace.teaming.authorization.dto.GoogleAccessTokenDto
import goodspace.teaming.authorization.dto.GoogleUserInfoResponseDto
import goodspace.teaming.global.security.TokenResponse
import goodspace.teaming.global.entity.user.*
import goodspace.teaming.global.repository.UserRepository
import goodspace.teaming.global.security.TokenProvider
import goodspace.teaming.global.security.TokenType
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class GoogleAuthService (
    private val restTemplate: RestTemplate,
    private val userRepository: UserRepository,
    private val toKenProvider: TokenProvider
) {
    fun googleSignInOrSingUp(googleAccessTokenDto: GoogleAccessTokenDto): TokenResponse {
        val googleUserInfo: GoogleUserInfoResponseDto = getGoogleUserInfo(googleAccessTokenDto.accessToken)

        val user = userRepository.findByIdentifierAndUserType(
            identifier = googleUserInfo.id,
            userType = UserType.GOOGLE
        ) ?: run {
            val newUser = OAuthUser(
                identifier = googleUserInfo.id,
                email = googleUserInfo.email,
                name = googleUserInfo.name,
                type = UserType.GOOGLE
            )
            userRepository.save(newUser)
        }

        val accessToken = toKenProvider.createToken(user.id!!, TokenType.ACCESS, listOf(Role.USER))
        val refreshToken = toKenProvider.createToken(user.id!!, TokenType.REFRESH, listOf(Role.USER))

        user.token = refreshToken
        userRepository.save(user)

        return TokenResponse(accessToken, refreshToken)
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