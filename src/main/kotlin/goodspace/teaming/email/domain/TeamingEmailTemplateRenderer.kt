package goodspace.teaming.email.domain

import org.springframework.stereotype.Component

@Component
class TeamingEmailTemplateRenderer : EmailTemplateRenderer {
    override fun renderEmailVerificationTemplate(
        code: String,
        expireMinute: Int
    ): String {
        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                  <meta charset="UTF-8">
                  <title>Teaming 이메일 인증</title>
                </head>
                <body style="margin:0; padding:40px 0; background-color:#ffffff; font-family:-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; color:#1a1a1a;">
                  <div style="max-width:600px; margin:0 auto; padding:0 20px;">
                    <header style="text-align:center; padding-bottom:24px; border-bottom:1px solid #e5e5e5;">
                      <h1 style="margin:0; font-size:24px; font-weight:600;">Teaming</h1>
                      <p style="margin-top:8px; font-size:14px; color:#666;">For Your Team</p>
                    </header>
    
                    <main style="padding:32px 0;">
                      <div style="margin:30px 0; padding:20px; background-color:#f5f5f5; border-radius:6px; text-align:center;">
                        <span style="font-size:28px; font-weight:700; letter-spacing:2px;">${code}</span>
                      </div>
                
                      <h2 style="font-size:20px; margin-bottom:16px;">이메일 인증 코드</h2>
                      <p style="font-size:16px; line-height:1.5;">안녕하세요! <strong>Teaming</strong>에 오신 것을 환영합니다.</p>
                      <p style="font-size:16px; line-height:1.5;">위 인증 코드를 입력해 주세요:</p>
                
                      <p style="font-size:14px; color:#666; margin-top: 5px">코드는 <strong>${expireMinute}분</strong> 뒤 만료됩니다.</p>
                    </main>
    
                    <footer style="border-top:1px solid #e5e5e5; padding-top:24px; font-size:12px; color:#888; text-align:center;">
                      <p style="margin:0;">본 메일은 발신 전용입니다.</p>
                      <p style="margin:8px 0 0;">© 2025 Good Space</p>
                    </footer>
                  </div>
                </body>
                </html>
                
                """.trimIndent()
    }
}
