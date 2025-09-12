package goodspace.teaming.email.event

import goodspace.teaming.email.domain.MailSender
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class EmailRequestListener(
    private val mailSender: MailSender
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(maxAttempts = 3, backoff = Backoff(delay = 1000))
    fun sendEmail(event: EmailSendRequestEvent) {
        mailSender.send(
            to = event.to,
            subject = event.subject,
            body = event.body
        )
    }
}
