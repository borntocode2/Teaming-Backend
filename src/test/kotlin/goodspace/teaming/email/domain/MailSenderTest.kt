package goodspace.teaming.email.domain

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetup
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl

class MailSenderTest {
    private lateinit var greenMail: GreenMail
    private lateinit var javaMailSender: JavaMailSender
    private lateinit var mailSender: MailSender

    @BeforeEach
    fun setup() {
        val serverSetup = ServerSetup(3025, null, ServerSetup.PROTOCOL_SMTP)
        greenMail = GreenMail(serverSetup)
        greenMail.start()

        // JavaMailSender가 GreenMail을 바라보도록 설정
        javaMailSender = JavaMailSenderImpl().apply {
            host = "localhost"
            port = serverSetup.port
            javaMailProperties["mail.smtp.auth"] = "false"
        }

        mailSender = MailSenderImpl(javaMailSender)
    }

    @AfterEach
    fun close() {
        greenMail.stop()
    }

    @Nested
    @DisplayName("send")
    inner class Send {
        @Test
        fun `이메일을 전송한다`() {
            // given
            val to = "test@email.com"
            val subject = "SUBJECT"
            val body = "EXAMPLE BODY"

            // when
            mailSender.send(
                to = to,
                subject = subject,
                body = body
            )

            greenMail.waitForIncomingEmail(1)

            // then
            val receivedMessages = greenMail.receivedMessages
            assertThat(receivedMessages).hasSize(1)

            val receivedMessage = receivedMessages[0]
            assertThat(receivedMessage.allRecipients[0].toString()).isEqualTo(to)
            assertThat(receivedMessage.subject).isEqualTo(subject)
            assertThat(receivedMessage.content as String).isEqualTo(body)
        }
    }
}
