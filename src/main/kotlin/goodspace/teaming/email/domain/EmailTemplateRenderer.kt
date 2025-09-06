package goodspace.teaming.email.domain

interface EmailTemplateRenderer {
    fun renderEmailVerificationTemplate(
        code: String,
        expireMinute: Int
    ): String
}
