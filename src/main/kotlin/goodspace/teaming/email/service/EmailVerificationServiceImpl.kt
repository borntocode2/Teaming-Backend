package goodspace.teaming.email.service

import goodspace.teaming.email.domain.CodeGenerator
import goodspace.teaming.email.domain.EmailTemplateRenderer
import goodspace.teaming.email.dto.CodeSendRequestDto
import goodspace.teaming.email.dto.EmailVerifyRequestDto
import goodspace.teaming.email.event.EmailSendRequestEvent
import goodspace.teaming.global.entity.email.EmailVerification
import goodspace.teaming.global.repository.EmailVerificationRepository
import goodspace.teaming.global.repository.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private const val MAIL_SUBJECT = "[Teaming] 이메일 인증 코드를 확인해주세요"

private const val EMAIL_NOT_FOUND = "이메일을 찾을 수 없습니다."
private const val EXPIRED = "이메일 인증이 만료되었습니다."
private const val WRONG_CODE = "코드가 올바르지 않습니다."
private const val NOT_EXISTS_EMAIL = "존재하지 않는 이메일입니다."
private const val DUPLICATE_EMAIL = "이미 존재하는 이메일입니다."
private const val CODE_LENGTH = 6
private const val EXPIRE_MINUTES = 5

@Service
class EmailVerificationServiceImpl(
    private val emailVerificationRepository: EmailVerificationRepository,
    private val userRepository: UserRepository,
    private val emailTemplateRenderer: EmailTemplateRenderer,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val codeGenerator: CodeGenerator
) : EmailVerificationService {
    @Transactional
    override fun publishVerificationCode(requestDto: CodeSendRequestDto) {
        val email = requestDto.email
        validateDuplicate(email, requestDto.shouldAlreadyExists)

        val code = codeGenerator.generate(CODE_LENGTH)
        val now = LocalDateTime.now()

        updateOrSave(
            email = email,
            code = code,
            expiresAt = now.plusMinutes(EXPIRE_MINUTES.toLong())
        )

        val body = emailTemplateRenderer.renderEmailVerificationTemplate(code, EXPIRE_MINUTES)

        applicationEventPublisher.publishEvent(
            EmailSendRequestEvent(
                to = email,
                subject = MAIL_SUBJECT,
                body = body
            )
        )
    }

    @Transactional
    override fun verifyEmail(requestDto: EmailVerifyRequestDto) {
        val email = requestDto.email

        val emailVerification = emailVerificationRepository.findByEmail(email)
            ?: throw EntityNotFoundException(EMAIL_NOT_FOUND)

        check(emailVerification.isNotExpired(LocalDateTime.now())) { EXPIRED }
        check(requestDto.code == emailVerification.code) { WRONG_CODE }

        emailVerification.verify()
    }

    private fun validateDuplicate(email: String, shouldExists: Boolean) {
        if (shouldExists) {
            require(isExistsEmail(email)) { NOT_EXISTS_EMAIL }
        }
        if (!shouldExists) {
            require(!isExistsEmail(email)) { DUPLICATE_EMAIL }
        }
    }

    private fun isExistsEmail(email: String): Boolean =
        userRepository.existsByEmail(email)

    private fun updateOrSave(
        email: String,
        code: String,
        expiresAt: LocalDateTime
    ) {
        emailVerificationRepository.findByEmail(email)
            ?.apply {
                this.code = code
                this.expiresAt = expiresAt
                this.verified = false
            }
            ?: emailVerificationRepository.save(EmailVerification(
                email = email,
                code = code,
                expiresAt = expiresAt,
            ))
    }
}
