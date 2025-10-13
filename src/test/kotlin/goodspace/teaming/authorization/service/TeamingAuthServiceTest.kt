package goodspace.teaming.authorization.service

import goodspace.teaming.authorization.dto.TeamingSignInRequestDto
import goodspace.teaming.authorization.dto.TeamingSignUpRequestDto
import goodspace.teaming.fixture.*
import goodspace.teaming.global.entity.user.User
import goodspace.teaming.global.exception.ALREADY_EXISTS_EMAIL
import goodspace.teaming.global.exception.NOT_VERIFIED_EMAIL
import goodspace.teaming.global.password.PasswordValidator
import goodspace.teaming.global.repository.EmailVerificationRepository
import goodspace.teaming.global.repository.UserRepository
import goodspace.teaming.global.security.TokenProvider
import goodspace.teaming.global.security.TokenType
import goodspace.teaming.util.createEmailVerification
import goodspace.teaming.util.createUser
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.util.ReflectionTestUtils

private const val ILLEGAL_PASSWORD = "illegal password"
private const val EXISTS_EMAIL = "exists@email.com"
private const val NOT_VERIFIED_EMAIL = "notVerified@email.com"
private const val RAW_PASSWORD = "rawPassword"
private const val ENCODED_PASSWORD = "encodedPassword"
private const val WRONG_PASSWORD = "wrongPassword"
private const val ACCESS_TOKEN = "accessToken"
private const val REFRESH_TOKEN = "refreshToken"
private const val EXISTS_TOKEN = "existsToken"

class TeamingAuthServiceTest {
    private val userRepository = mockk<UserRepository>()
    private val emailVerificationRepository = mockk<EmailVerificationRepository>(relaxed = true)
    private val tokenProvider = mockk<TokenProvider>(relaxed = true)
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val passwordValidator = mockk<PasswordValidator>()

    private val teamingAuthService = TeamingAuthService(
        userRepository = userRepository,
        emailVerificationRepository = emailVerificationRepository,
        tokenProvider = tokenProvider,
        passwordEncoder = passwordEncoder,
        passwordValidator = passwordValidator
    )

    @Nested
    @DisplayName("signUp")
    inner class SignUp {
        @Test
        fun `새로운 회원을 생성한다`() {
            // given
            val requestDto = createSignUpRequestDto()
            val emailVerification = createEmailVerification(verified = true)

            every { passwordValidator.isIllegalPassword(requestDto.password) } returns false
            every { userRepository.existsByEmail(requestDto.email) } returns false
            every { emailVerificationRepository.findByEmail(requestDto.email) } returns emailVerification
            every { passwordEncoder.encode(any()) } answers { ENCODED_PASSWORD }

            every { userRepository.save(any<User>()) } answers {
                val user = firstArg<User>()
                setId(user, USER_ID)
                user
            }

            // when
            teamingAuthService.signUp(requestDto)

            // then
            verify(exactly = 1) {
                userRepository.save(withArg<User> { createdUser ->
                    assertThat(createdUser.email).isEqualTo(requestDto.email)
                    assertThat(createdUser.name).isEqualTo(requestDto.name)
                })
            }
        }

        @Test
        fun `생성된 회원에 대한 토큰을 반환한다`() {
            // given
            val requestDto = createSignUpRequestDto()
            val emailVerification = createEmailVerification(verified = true)
            val expectedUser = createUser()

            every { passwordValidator.isIllegalPassword(requestDto.password) } returns false
            every { userRepository.existsByEmail(requestDto.email) } returns false
            every { emailVerificationRepository.findByEmail(requestDto.email) } returns emailVerification
            every { passwordEncoder.encode(any()) } answers { firstArg() }
            every { userRepository.save(any()) } returns expectedUser

            // when
            teamingAuthService.signUp(requestDto)

            // then
            verify(exactly = 1) { tokenProvider.createToken(expectedUser.id!!, TokenType.ACCESS, expectedUser.roles) }
            verify(exactly = 1) { tokenProvider.createToken(expectedUser.id!!, TokenType.REFRESH, expectedUser.roles) }
        }

        @Test
        fun `비밀번호를 암호화하여 저장한다`() {
            // given
            val requestDto = createSignUpRequestDto(password = RAW_PASSWORD)
            val emailVerification = createEmailVerification(verified = true)

            every { passwordValidator.isIllegalPassword(requestDto.password) } returns false
            every { userRepository.existsByEmail(requestDto.email) } returns false
            every { emailVerificationRepository.findByEmail(requestDto.email) } returns emailVerification
            every { passwordEncoder.encode(RAW_PASSWORD) } answers { ENCODED_PASSWORD }
            every { userRepository.save(any()) } answers { createUser() }

            // when
            teamingAuthService.signUp(requestDto)

            // then
            verify(exactly = 1) { passwordEncoder.encode(RAW_PASSWORD) }
        }

        @Test
        fun `비밀번호의 형식이 부적절하면 예외를 던진다`() {
            // given
            val requestDto = createSignUpRequestDto(password = ILLEGAL_PASSWORD)

            every { passwordValidator.isIllegalPassword(ILLEGAL_PASSWORD) } returns true

            // when & then
            assertThatThrownBy { teamingAuthService.signUp(requestDto) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage(goodspace.teaming.global.exception.ILLEGAL_PASSWORD)
        }

        @Test
        fun `다른 회원이 이미 사용 중인 이메일이라면 예외를 던진다`() {
            // given
            val requestDto = createSignUpRequestDto(email = EXISTS_EMAIL)

            every { passwordValidator.isIllegalPassword(requestDto.password) } returns false
            every { userRepository.existsByEmail(EXISTS_EMAIL) } returns true

            // when & then
            assertThatThrownBy { teamingAuthService.signUp(requestDto) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage(ALREADY_EXISTS_EMAIL)
        }

        @Test
        fun `이메일이 인증되지 않았다면 예외를 던진다`() {
            // given
            val requestDto = createSignUpRequestDto(email = NOT_VERIFIED_EMAIL)
            val emailVerification = createEmailVerification(
                email = NOT_VERIFIED_EMAIL,
                verified = false
            )

            every { passwordValidator.isIllegalPassword(requestDto.password) } returns false
            every { userRepository.existsByEmail(NOT_VERIFIED_EMAIL) } returns false
            every { emailVerificationRepository.findByEmail(NOT_VERIFIED_EMAIL) } returns emailVerification

            // when & then
            assertThatThrownBy { teamingAuthService.signUp(requestDto) }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage(NOT_VERIFIED_EMAIL)
        }
    }

    @Nested
    @DisplayName("signIn")
    inner class SignIn {
        @Test
        fun `이메일과 비밀번호가 일치한다면 토큰을 반환한다`() {
            // given
            val user = createUser(password = ENCODED_PASSWORD)
            val requestDto = TeamingSignInRequestDto(user.email, RAW_PASSWORD)

            every { userRepository.findTeamingUserByEmail(user.email) } returns user
            every { passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD) } returns true
            every { tokenProvider.createToken(user.id!!, TokenType.ACCESS, user.roles) } returns ACCESS_TOKEN
            every { tokenProvider.createToken(user.id!!, TokenType.REFRESH, user.roles) } returns REFRESH_TOKEN

            // when
            val response = teamingAuthService.signIn(requestDto)

            // then
            assertThat(response.accessToken).isEqualTo(ACCESS_TOKEN)
            assertThat(response.refreshToken).isEqualTo(REFRESH_TOKEN)
        }

        @Test
        fun `회원의 리프레쉬 토큰을 갱신한다`() {
            // given
            val user = createUser(password = ENCODED_PASSWORD)
            user.token = EXISTS_TOKEN
            val requestDto = TeamingSignInRequestDto(user.email, RAW_PASSWORD)

            every { userRepository.findTeamingUserByEmail(user.email) } returns user
            every { passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD) } returns true
            every { tokenProvider.createToken(user.id!!, TokenType.ACCESS, user.roles) } returns ACCESS_TOKEN
            every { tokenProvider.createToken(user.id!!, TokenType.REFRESH, user.roles) } returns REFRESH_TOKEN

            // when
            teamingAuthService.signIn(requestDto)

            // then
            assertThat(user.token).isNotEqualTo(EXISTS_TOKEN)
            assertThat(user.token).isEqualTo(REFRESH_TOKEN)
        }

        @Test
        fun `비밀번호가 일치하지 않으면 예외를 던진다`() {
            // given
            val user = createUser(password = ENCODED_PASSWORD)
            val requestDto = TeamingSignInRequestDto(user.email, WRONG_PASSWORD)

            every { userRepository.findTeamingUserByEmail(user.email) } returns user
            every { passwordEncoder.matches(WRONG_PASSWORD, ENCODED_PASSWORD) } returns false

            // when & then
            assertThatThrownBy { teamingAuthService.signIn(requestDto) }
                .isInstanceOf(IllegalArgumentException::class.java)
        }
    }

    private fun createSignUpRequestDto(
        email: String = USER_EMAIL,
        password: String = USER_PASSWORD,
        name: String = USER_NAME
    ): TeamingSignUpRequestDto {
        return TeamingSignUpRequestDto(
            email = email,
            password = password,
            name = name
        )
    }

    private fun setId(user: User, id: Long) {
        ReflectionTestUtils.setField(user, "id", id)
    }
}
