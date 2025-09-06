package goodspace.teaming.email.domain

import org.springframework.stereotype.Component
import java.security.SecureRandom

@Component
class RandomCodeGenerator : CodeGenerator {
    private val secureRandom = SecureRandom()

    override fun generate(length: Int): String {
        val sb = StringBuilder(length)

        for (i in 0 until length) {
            sb.append(secureRandom.nextInt(10))
        }

        return sb.toString()
    }
}
