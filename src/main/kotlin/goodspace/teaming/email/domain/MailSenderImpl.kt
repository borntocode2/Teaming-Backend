package goodspace.teaming.email.domain

import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component

@Component
class MailSenderImpl(
    private val javaMailSender: JavaMailSender
) : MailSender {
    override fun send(to: String, subject: String, body: String) {
        val message: MimeMessage = javaMailSender.createMimeMessage()

        val helper = MimeMessageHelper(message, "UTF-8")
        helper.setTo(to)
        helper.setSubject(subject)
        helper.setText(body, true)

        javaMailSender.send(message)
    }
}
