package goodspace.teaming.chat.domain

import org.springframework.stereotype.Component
import java.security.SecureRandom

private const val SHORT_LENGTH = "지정된 코드 길이가 너무 짧습니다."
private const val INSUFFICIENT_CHARACTERS = "허용된 문자가 너무 적습니다."

@Component
class InviteCodeGenerator(
    private val codeLength: Int = 10,
    private val allowedCharacters: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789",
    private val random: SecureRandom = SecureRandom()
) {
    init {
        require(codeLength > 0) { SHORT_LENGTH }
        require(allowedCharacters.isNotEmpty()) { INSUFFICIENT_CHARACTERS }
    }

    fun generate(): String = buildString(codeLength) {
        repeat(codeLength) {
            append(allowedCharacters[random.nextInt(allowedCharacters.length)])
        }
    }
}
