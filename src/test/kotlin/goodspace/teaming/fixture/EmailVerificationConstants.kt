package goodspace.teaming.fixture

import java.time.LocalDateTime

const val EMAIL_VERIFICATION_ID = 3456L
const val EMAIL_VERIFICATION_EMAIL = "emailVerification@email.com"
const val EMAIL_VERIFICATION_CODE = "123456"
val EMAIL_VERIFICATION_EXPIRES_AT: LocalDateTime = LocalDateTime.now().plusMinutes(5)
