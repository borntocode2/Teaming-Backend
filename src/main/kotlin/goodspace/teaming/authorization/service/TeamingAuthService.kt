package goodspace.teaming.authorization.service

import goodspace.teaming.authorization.dto.TeamingSignInRequestDto
import goodspace.teaming.authorization.dto.TeamingSignUpRequestDto
import goodspace.teaming.global.entity.user.Role
import goodspace.teaming.global.entity.user.TeamingUser
import goodspace.teaming.global.password.PasswordValidator
import goodspace.teaming.global.repository.EmailVerificationRepository
import goodspace.teaming.global.repository.UserRepository
import goodspace.teaming.global.security.TokenProvider
import goodspace.teaming.global.security.TokenResponseDto
import goodspace.teaming.global.security.TokenType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private const val USER_NOT_FOUND = "회원을 조회할 수 없습니다."
private const val ILLEGAL_PASSWORD = "부적절한 비밀번호입니다."
private const val EXISTS_EMAIL = "이미 사용중인 이메일입니다."
private const val NOT_VERIFIED_EMAIL = "인증되지 않은 이메일입니다."

@Service
class TeamingAuthService(
    private val userRepository: UserRepository,
    private val emailVerificationRepository: EmailVerificationRepository,
    private val tokenProvider: TokenProvider,
    private val passwordEncoder: PasswordEncoder,
    private val passwordValidator: PasswordValidator
) {
    @Transactional
    fun signUp(
        requestDto: TeamingSignUpRequestDto
    ): TokenResponseDto {
        assertPassword(requestDto.password)
        assertEmail(requestDto.email)

        val user = saveNewUserFrom(requestDto)

        val accessToken = tokenProvider.createToken(user.id!!, TokenType.ACCESS, user.roles)
        val refreshToken = tokenProvider.createToken(user.id!!, TokenType.REFRESH, user.roles)

        user.token = refreshToken

        return TokenResponseDto(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    @Transactional
    fun signIn(
        requestDto: TeamingSignInRequestDto
    ): TokenResponseDto {
        val email = requestDto.email

        val user = userRepository.findTeamingUserByEmail(email)
            ?: throw IllegalArgumentException(USER_NOT_FOUND)

        require(passwordEncoder.matches(requestDto.password, user.password)) { USER_NOT_FOUND }

        val accessToken = tokenProvider.createToken(user.id!!, TokenType.ACCESS, user.roles)
        val refreshToken = tokenProvider.createToken(user.id!!, TokenType.REFRESH, user.roles)

        user.token = refreshToken

        return TokenResponseDto(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    private fun assertPassword(rawPassword: String) {
        require(!passwordValidator.isIllegalPassword(rawPassword)) { ILLEGAL_PASSWORD }
    }

    private fun assertEmail(email: String) {
        require(!userRepository.existsByEmail(email)) { EXISTS_EMAIL }

        val emailVerification = emailVerificationRepository.findByEmail(email)
            ?: throw IllegalStateException(NOT_VERIFIED_EMAIL)

        check(emailVerification.verified) { NOT_VERIFIED_EMAIL }

        emailVerificationRepository.delete(emailVerification)
    }

    private fun saveNewUserFrom(requestDto: TeamingSignUpRequestDto): TeamingUser {
        val (email, password, name, avatarKey, avatarVersion) = requestDto

        val user = TeamingUser(
            email = email,
            password = passwordEncoder.encode(password),
            name = name,
            avatarKey = avatarKey,
            avatarVersion = avatarVersion
        )
        user.addRole(Role.USER)

        return userRepository.save(user)
    }
}
