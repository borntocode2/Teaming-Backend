package goodspace.teaming.authorization.service

import goodspace.teaming.authorization.dto.AccessTokenReissueRequestDto
import goodspace.teaming.authorization.dto.AccessTokenResponseDto
import goodspace.teaming.global.entity.user.User
import goodspace.teaming.global.repository.UserRepository
import goodspace.teaming.global.security.TokenProvider
import goodspace.teaming.global.security.TokenType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private const val USER_NOT_FOUND = "회원을 조회할 수 없습니다."
private const val ILLEGAL_TOKEN = "부적절한 토큰입니다."
private const val EXPIRED_REFRESH_TOKEN = "만료된 리프레쉬 토큰입니다."

@Service
class TokenManagementService(
    private val userRepository: UserRepository,
    private val tokenProvider: TokenProvider
) {
    @Transactional(readOnly = true)
    fun reissueAccessToken(requestDto: AccessTokenReissueRequestDto): AccessTokenResponseDto {
        val refreshToken = requestDto.refreshToken

        val userId = tokenProvider.getIdFromToken(refreshToken)
        val user = findUser(userId)

        assertRefreshToken(user, refreshToken)

        val accessToken = tokenProvider.createToken(user.id!!, TokenType.ACCESS, user.roles)

        return AccessTokenResponseDto(accessToken)
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
