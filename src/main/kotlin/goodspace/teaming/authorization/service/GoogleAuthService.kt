package goodspace.teaming.authorization.service

import goodspace.teaming.authorization.config.GoogleProperties
import org.springframework.web.client.RestTemplate

class GoogleAuthService (
    private val googleProperties : GoogleProperties,
    private val restTemplate: RestTemplate = RestTemplate()
) {
    // 1. 사용자에게 구글로그인 폼 전송
    fun getGoogleLoginUrl(): String {
        val base = "https://www.googleapis.com/oauth2/v2/token"
        val scope = "https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile"

        return "$base?client_id=${googleProperties.clientId}" +
                "&redirect_uri=${googleProperties.redirectUri}" +
                "&response_type=code" +
                "&scope=$scope"
    }

    fun getAccessTokenFromCode(code: String): String {
        val tokenUrl = "https://oauth2.googleapis.com/token"
        val params = mapOf(
            "code" to code,
            "client_id" to googleProperties.clientId,
            "redirect_uri" to googleProperties.redirectUri,
            "client_secret" to googleProperties.clientSecret,
            "grant_type" to "authorization_code",
        )
        val tokenResponse = restTemplate.postForEntity(tokenUrl, params, Map::class.java).body!!
        val accessToken = tokenResponse["access_token"] as String

        return accessToken
    }


}