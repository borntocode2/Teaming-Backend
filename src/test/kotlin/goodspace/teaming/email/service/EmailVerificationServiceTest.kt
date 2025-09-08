package goodspace.teaming.email.service

import goodspace.teaming.email.dto.CodeSendRequestDto
import goodspace.teaming.email.dto.EmailVerifyRequestDto
import goodspace.teaming.email.event.EmailSendRequestEvent
import goodspace.teaming.global.entity.email.EmailVerification
import goodspace.teaming.global.entity.user.TeamingUser
import goodspace.teaming.global.repository.EmailVerificationRepository
import goodspace.teaming.global.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.event.ApplicationEvents
import org.springframework.test.context.event.RecordApplicationEvents
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private const val NOT_EXIST_EMAIL = "notExist@email.com"
private const val DEFAULT_EMAIL = "default@email.com"
private const val DEFAULT_CODE = "defaultCode"
private const val DEFAULT_NAME = "DEFAULT NAME"
private const val DEFAULT_PASSWORD = "defaultPassword"

@SpringBootTest
@RecordApplicationEvents
@Transactional
class EmailVerificationServiceTest(
    @Autowired
    private val emailVerificationService: EmailVerificationService,
    @Autowired
    private val emailVerificationRepository: EmailVerificationRepository,
    @Autowired
    private val userRepository: UserRepository
) {
    @Nested
    @DisplayName("publishVerificationCode")
    inner class PublishVerificationCode {
        @Test
        fun `이메일 인증 객체를 생성한다`() {
            // given
            val requestDto = CodeSendRequestDto(NOT_EXIST_EMAIL, false)

            // when
            emailVerificationService.publishVerificationCode(requestDto)

            // then
            val emailVerification = emailVerificationRepository.findByEmail(NOT_EXIST_EMAIL)
            assertThat(emailVerification).isNotNull()
        }

        @Test
        fun `shouldAlreadyExists가 false라면, 회원이 이미 사용 중인 이메일일 떄 예외가 발생한다`() {
            // arrange
            val existEmail = "EXIST@email.com"
            userRepository.save(
                TeamingUser(
                    email = existEmail,
                    password = DEFAULT_PASSWORD,
                    name = DEFAULT_NAME
                )
            )

            val requestDto = CodeSendRequestDto(
                email = existEmail,
                shouldAlreadyExists = false
            )

            // act
            assertThatThrownBy { emailVerificationService.publishVerificationCode(requestDto) }
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun `shouldAlreadyExists가 true라면, 회원이 사용 중이지 않은 이메일일 떄 예외가 발생한다`() {
            // given
            val requestDto = CodeSendRequestDto(
                email = NOT_EXIST_EMAIL,
                shouldAlreadyExists = true
            )

            // act
            assertThatThrownBy { emailVerificationService.publishVerificationCode(requestDto) }
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun `이메일 발송 요청 이벤트가 발생한다`(applicationEvents: ApplicationEvents) {
            // given
            val email = "EMAIL@email.com"
            val requestDto = CodeSendRequestDto(email, false)

            // when
            emailVerificationService.publishVerificationCode(requestDto)

            // then
            val events = applicationEvents.stream(EmailSendRequestEvent::class.java).toList()
            assertThat(events).hasSize(1)
            assertThat(events.first().to).isEqualTo(email)
        }
    }

    @Nested
    @DisplayName("verifyEmail")
    inner class VerifyEmail {
        @Test
        fun `생성된 이메일 인증 객체의 코드와 일치하면, 객체의 인증 상태를 true로 한다`() {
            // arrange
            val emailVerification = emailVerificationRepository.save(
                EmailVerification(
                    email = DEFAULT_EMAIL,
                    code = DEFAULT_CODE,
                    expiresAt = LocalDateTime.now().plusMinutes(1)
                )
            )
            val requestDto = EmailVerifyRequestDto(DEFAULT_EMAIL, DEFAULT_CODE)

            // act
            emailVerificationService.verifyEmail(requestDto)

            // assert
            assertThat(emailVerification.verified).isTrue()
        }

        @Test
        fun `이메일 인증 객체가 만료되었다면 예외를 던진다`() {
            // arrange
            emailVerificationRepository.save(
                EmailVerification(
                    email = DEFAULT_EMAIL,
                    code = DEFAULT_CODE,
                    expiresAt = LocalDateTime.now().minusMinutes(1)
                )
            )
            val requestDto = EmailVerifyRequestDto(DEFAULT_EMAIL, DEFAULT_CODE)

            // assert
            assertThatThrownBy { emailVerificationService.verifyEmail(requestDto) }
                .isInstanceOf(IllegalStateException::class.java)
        }
    }
}
