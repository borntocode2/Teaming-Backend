package goodspace.teaming.user.service

import goodspace.teaming.fixture.*
import goodspace.teaming.global.entity.email.EmailVerification
import goodspace.teaming.global.entity.user.TeamingUser
import goodspace.teaming.global.password.PasswordValidatorImpl
import goodspace.teaming.global.repository.EmailVerificationRepository
import goodspace.teaming.global.repository.UserRepository
import goodspace.teaming.user.domain.mapper.UserInfoMapper
import goodspace.teaming.user.dto.UpdateEmailRequestDto
import goodspace.teaming.user.dto.UpdateNameRequestDto
import goodspace.teaming.user.dto.UpdatePasswordRequestDto
import goodspace.teaming.user.dto.UserInfoResponseDto
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime
import java.util.*

private const val NEW_EMAIL = "new@email.com"
private const val NEW_NAME = "new name"
private const val NEW_PASSWORD = "newPassword"
private const val EXISTS_EMAIL = "exists@email.com"
private const val ENCODED_PASSWORD = "encodedPassword"
private const val WRONG_PASSWORD = "worngPassword"
private const val ILLEGAL_PASSWORD = "illegalPassword"

class UserServiceTest {
    private val userRepository = mockk<UserRepository>()
    private val emailVerificationRepository = mockk<EmailVerificationRepository>(relaxed = true)
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val passwordValidator = mockk<PasswordValidatorImpl>()
    private val userInfoMapper = mockk<UserInfoMapper>()

    private val userService = UserServiceImpl(
        userRepository = userRepository,
        emailVerificationRepository = emailVerificationRepository,
        passwordEncoder = passwordEncoder,
        passwordValidator = passwordValidator,
        userInfoMapper = userInfoMapper
    )

    @Nested
    @DisplayName("getUserInfo")
    inner class GetUserInfo {
        @Test
        fun `회원 정보를 DTO로 매핑해 반환한다`() {
            // given
            val user = createUser(id = USER_ID)
            val userId = user.id!!
            val expectedResponse = mockk<UserInfoResponseDto>()

            every { userRepository.findById(userId) } returns Optional.of(user)
            every { userInfoMapper.map(user) } returns expectedResponse

            // when
            val actualResponse = userService.getUserInfo(userId)

            // then
            assertThat(actualResponse).isEqualTo(expectedResponse)
        }
    }

    @Nested
    @DisplayName("updateEmail")
    inner class UpdateEmail {
        @Test
        fun `회원의 이메일을 수정한다`() {
            // given
            val user = createUser(email = USER_EMAIL)
            val userId = user.id!!
            val emailVerification = createEmailVerification(
                email = NEW_EMAIL,
                verified = true
            )

            val requestDto = UpdateEmailRequestDto(email = NEW_EMAIL)

            every { userRepository.findById(userId) } returns Optional.of(user)
            every { userRepository.existsByEmail(NEW_EMAIL) } returns false
            every { emailVerificationRepository.findByEmail(NEW_EMAIL) } returns emailVerification

            // when
            userService.updateEmail(userId, requestDto)

            // then
            assertThat(user.email).isNotEqualTo(USER_EMAIL)
            assertThat(user.email).isEqualTo(NEW_EMAIL)
        }

        @Test
        fun `이미 사용 중인 이메일이라면 예외가 발생한다`() {
            // given
            val user = createUser()
            val userId = user.id!!

            val requestDto = UpdateEmailRequestDto(email = EXISTS_EMAIL)

            every { userRepository.findById(userId) } returns Optional.of(user)
            every { userRepository.existsByEmail(EXISTS_EMAIL) } returns true

            // when & then
            assertThatThrownBy { userService.updateEmail(userId, requestDto) }
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun `이메일 인증 객체가 없다면 예외가 발생한다`() {
            // given
            val user = createUser()
            val userId = user.id!!

            val requestDto = UpdateEmailRequestDto(email = NEW_EMAIL)

            every { userRepository.findById(userId) } returns Optional.of(user)
            every { userRepository.existsByEmail(NEW_EMAIL) } returns false
            every { emailVerificationRepository.findByEmail(NEW_EMAIL) } returns null

            // when & then
            assertThatThrownBy { userService.updateEmail(userId, requestDto) }
                .isInstanceOf(IllegalStateException::class.java)
        }

        @Test
        fun `이메일 인증 객체의 인증 여부가 false라면 예외가 발생한다`() {
            // given
            val user = createUser(email = USER_EMAIL)
            val userId = user.id!!
            val emailVerification = createEmailVerification(
                email = NEW_EMAIL,
                verified = false
            )

            val requestDto = UpdateEmailRequestDto(email = NEW_EMAIL)

            every { userRepository.findById(userId) } returns Optional.of(user)
            every { userRepository.existsByEmail(NEW_EMAIL) } returns false
            every { emailVerificationRepository.findByEmail(NEW_EMAIL) } returns emailVerification

            // when & then
            assertThatThrownBy { userService.updateEmail(userId, requestDto) }
                .isInstanceOf(IllegalStateException::class.java)
        }
    }

    @Nested
    @DisplayName("updatePassword")
    inner class UpdatePassword {
        @Test
        fun `비밀번호를 수정한다`() {
            // given
            val user = createUser()
            val currentPassword = user.password

            val requestDto = UpdatePasswordRequestDto(
                currentPassword = currentPassword,
                newPassword = NEW_PASSWORD
            )

            every { userRepository.findTeamingUserById(user.id!!) } returns user
            every { passwordEncoder.encode(any()) } answers { firstArg() }
            every { passwordValidator.isIllegalPassword(NEW_PASSWORD) } returns false

            // when
            userService.updatePassword(user.id!!, requestDto)

            // then
            assertThat(user.password).isNotEqualTo(currentPassword)
            assertThat(user.password).isEqualTo(NEW_PASSWORD)
        }

        @Test
        fun `비밀번호를 암호화해서 저장한다`() {
            // given
            val user = createUser()
            val currentPassword = user.password

            val requestDto = UpdatePasswordRequestDto(
                currentPassword = currentPassword,
                newPassword = NEW_PASSWORD
            )

            every { userRepository.findTeamingUserById(user.id!!) } returns user
            every { passwordEncoder.encode(currentPassword) } returns currentPassword
            every { passwordValidator.isIllegalPassword(NEW_PASSWORD) } returns false
            every { passwordEncoder.encode(NEW_PASSWORD) } returns ENCODED_PASSWORD

            // when
            userService.updatePassword(user.id!!, requestDto)

            // then
            assertThat(user.password).isNotEqualTo(currentPassword)
            assertThat(user.password).isEqualTo(ENCODED_PASSWORD)
        }

        @Test
        fun `기존 비밀번호가 일치하지 않는다면 예외를 던진다`() {
            // given
            val user = createUser()

            val requestDto = UpdatePasswordRequestDto(
                currentPassword = WRONG_PASSWORD,
                newPassword = NEW_PASSWORD
            )

            every { userRepository.findTeamingUserById(user.id!!) } returns user
            every { passwordEncoder.encode(any()) } answers { firstArg() }

            // when & then
            assertThatThrownBy { userService.updatePassword(user.id!!, requestDto) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("비밀번호가 올바르지 않습니다.")
        }

        @Test
        fun `비밀번호 형식이 부적절하다면 예외를 던진다`() {
            // given
            val user = createUser()
            val currentPassword = user.password

            val requestDto = UpdatePasswordRequestDto(
                currentPassword = currentPassword,
                newPassword = ILLEGAL_PASSWORD
            )

            every { userRepository.findTeamingUserById(user.id!!) } returns user
            every { passwordEncoder.encode(any()) } answers { firstArg() }
            every { passwordValidator.isIllegalPassword(ILLEGAL_PASSWORD) } returns true

            // when & then
            assertThatThrownBy { userService.updatePassword(user.id!!, requestDto) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("부적절한 비밀번호입니다.")
        }
    }

    @Nested
    @DisplayName("updateName")
    inner class UpdateName {
        @Test
        fun `회원의 이름을 수정한다`() {
            // given
            val user = createUser()
            val originalName = user.name
            val requestDto = UpdateNameRequestDto(name = NEW_NAME)

            every { userRepository.findById(user.id!!) } returns Optional.of(user)

            // when
            userService.updateName(user.id!!, requestDto)

            // then
            assertThat(user.name).isNotEqualTo(originalName)
            assertThat(user.name).isEqualTo(NEW_NAME)
        }
    }

    private fun createUser(
        email: String = USER_EMAIL,
        name: String = USER_NAME,
        password: String = USER_PASSWORD,
        id: Long = USER_ID
    ): TeamingUser {
        val user = TeamingUser(
            email = email,
            name = name,
            password = password
        )
        ReflectionTestUtils.setField(user, "id", id)

        return user
    }

    private fun createEmailVerification(
        email: String = NEW_EMAIL,
        verified: Boolean = true,
        code: String = EMAIL_VERIFICATION_CODE,
        expiresAt: LocalDateTime = EMAIL_VERIFICATION_EXPIRES_AT,
        id: Long = EMAIL_VERIFICATION_ID
    ): EmailVerification {
        val emailVerification = EmailVerification(
            email = email,
            verified = verified,
            code = code,
            expiresAt = expiresAt
        )
        ReflectionTestUtils.setField(emailVerification, "id", id)

        return emailVerification
    }
}
