package goodspace.teaming.user.service

import goodspace.teaming.authorization.dto.AccessTokenReissueRequestDto
import goodspace.teaming.authorization.dto.AccessTokenResponseDto
import goodspace.teaming.global.entity.user.User
import goodspace.teaming.global.exception.EXPIRED_REFRESH_TOKEN
import goodspace.teaming.global.exception.ILLEGAL_TOKEN
import goodspace.teaming.global.exception.USER_NOT_FOUND
import goodspace.teaming.global.repository.UserRepository
import goodspace.teaming.global.security.TokenProvider
import goodspace.teaming.global.security.TokenType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TokenManagementServiceImpl(
    private val userRepository: UserRepository,
    private val tokenProvider: TokenProvider
) : TokenManagementService {
    @Transactional(readOnly = true)
    override fun reissueAccessToken(requestDto: AccessTokenReissueRequestDto): AccessTokenResponseDto {
        val refreshToken = requestDto.refreshToken

        val userId = tokenProvider.getIdFromToken(refreshToken)
        val user = findUser(userId)

        assertRefreshToken(user, refreshToken)

        val accessToken = tokenProvider.createToken(user.id!!, TokenType.ACCESS, user.roles)

        return AccessTokenResponseDto(accessToken)
    }

    @Transactional
    override fun expireRefreshToken(userId: Long) {
        val user = findUser(userId)

        user.token = null
    }

    private fun assertRefreshToken(user: User, refreshToken: String) {
        require(tokenProvider.validateToken(refreshToken, TokenType.REFRESH)) { ILLEGAL_TOKEN }
        require(user.token == refreshToken) { EXPIRED_REFRESH_TOKEN }
    }

    private fun findUser(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException(USER_NOT_FOUND) }
    }
}
