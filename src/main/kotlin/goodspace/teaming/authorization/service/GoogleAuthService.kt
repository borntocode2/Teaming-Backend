package goodspace.teaming.authorization.service

import goodspace.teaming.authorization.config.GoogleProperties
import goodspace.teaming.authorization.dto.GoogleAccessTokenDto
import goodspace.teaming.authorization.dto.GoogleUserInfoResponseDto
import goodspace.teaming.authorization.dto.TokenResponseDto
import goodspace.teaming.global.entity.user.OAuthUser
import goodspace.teaming.global.entity.user.TeamingUser
import goodspace.teaming.global.entity.user.User
import goodspace.teaming.global.entity.user.UserType
import goodspace.teaming.global.repository.UserRepository
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestTemplate

class GoogleAuthService (
    private val restTemplate: RestTemplate = RestTemplate(),
    private val userRepository: UserRepository
) {
    fun getAccessTokenFromGoogleAccessToken(googleAccessTokenDto: GoogleAccessTokenDto): TokenResponseDto {
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

        return response.body!!
    }

}