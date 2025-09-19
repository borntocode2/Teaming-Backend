package goodspace.teaming.authorization.service

import goodspace.teaming.global.repository.UserRepository
import goodspace.teaming.global.security.TokenProvider
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class KakaoAuthService (
    private val restTemplate: RestTemplate,
    private val userRepository: UserRepository,
    private val toKenProvider: TokenProvider
) {

}