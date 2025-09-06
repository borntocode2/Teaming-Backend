package goodspace.teaming.email.domain

interface MailSender {
    fun send(to: String, subject: String, body: String)
}
