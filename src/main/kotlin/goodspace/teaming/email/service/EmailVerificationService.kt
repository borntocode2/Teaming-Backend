package goodspace.teaming.email.service

import goodspace.teaming.email.dto.CodeSendRequestDto
import goodspace.teaming.email.dto.EmailVerifyRequestDto

interface EmailVerificationService {
    fun publishVerificationCode(requestDto: CodeSendRequestDto)

    fun verifyEmail(requestDto: EmailVerifyRequestDto)
}
