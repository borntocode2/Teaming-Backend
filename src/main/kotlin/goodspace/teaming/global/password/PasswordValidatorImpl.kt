package goodspace.teaming.global.password

import org.springframework.stereotype.Component
import java.util.*

@Component
class PasswordValidatorImpl(
    private val minimumLength: Int = 8
) : PasswordValidator {
    override fun isIllegalPassword(password: String): Boolean {
        return isShortThanMiniMum(password) ||
                isNotContainAlphabetic(password) ||
                isNotContainDigit(password) ||
                isNotContainSpecialCharacter(password) ||
                isEmailFormat(password) ||
                hasSequentialCharacters(password)
    }

    private fun isShortThanMiniMum(password: String): Boolean {
        return password.length < minimumLength
    }

    private fun isNotContainAlphabetic(password: String): Boolean {
        for (word in password.toCharArray()) {
            if (Character.isAlphabetic(word.code)) {
                return false
            }
        }

        return true
    }

    private fun isNotContainDigit(password: String): Boolean {
        for (word in password.toCharArray()) {
            if (Character.isDigit(word)) {
                return false
            }
        }

        return true
    }

    private fun isNotContainSpecialCharacter(password: String): Boolean {
        for (word in password.toCharArray()) {
            if (specialChars.contains(word)) {
                return false
            }
        }

        return true
    }

    private fun isEmailFormat(password: String): Boolean {
        return password.contains("@")
    }

    private fun hasSequentialCharacters(password: String): Boolean {
        val lower = password.lowercase(Locale.getDefault())
        for (i in 0 until lower.length - 2) {
            val c1 = lower[i]
            val c2 = lower[i + 1]
            val c3 = lower[i + 2]

            if (c2.code == c1.code + 1 && c3.code == c2.code + 1) {
                return true
            }
            if (c2.code == c1.code - 1 && c3.code == c2.code - 1) {
                return true
            }
        }

        return false
    }

    companion object {
        private val specialChars = setOf(
            '!', '#', '$', '%', '^', '&', '*',
            '(', ')', '-', '_', '=', '+', '[',
            ']', '{', '}', ';', ':', '\'', '"',
            ',', '.', '<', '>', '/', '?', '\\',
            '`', '~'
        )
    }
}
