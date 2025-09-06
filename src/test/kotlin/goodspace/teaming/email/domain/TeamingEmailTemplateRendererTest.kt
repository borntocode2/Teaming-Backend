package goodspace.teaming.email.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private const val DEFAULT_CODE = "DEFAULT_CODE"
private const val DEFAULT_EXPIRE_MINUTE = 5

class TeamingEmailTemplateRendererTest {
    private val teamingEmailTemplateRenderer = TeamingEmailTemplateRenderer()

    @Nested
    @DisplayName("renderEmailVerificationTemplate")
    inner class RenderEmailVerificationTemplate {
        @Test
        fun `코드와 만료일자를 담은 이메일 본문을 반환한다`() {
            // given
            val code = DEFAULT_CODE
            val expireMinute = DEFAULT_EXPIRE_MINUTE

            // when
            val emailContent = teamingEmailTemplateRenderer.renderEmailVerificationTemplate(
                code = code,
                expireMinute = expireMinute
            )

            // then
            assertThat(emailContent).contains(code)
            assertThat(emailContent).contains(expireMinute.toString())
        }
    }
}
