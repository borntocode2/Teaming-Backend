package goodspace.teaming.global.password

import org.springframework.stereotype.Component

private const val MINIMUM_LENGTH = 8

@Component
class PasswordValidator{
    private val specialChars = setOf(
        '!', '#', '$', '%', '^', '&', '*',
        '(', ')', '-', '_', '=', '+', '[',
        ']', '{', '}', ';', ':', '\'', '"',
        ',', '.', '<', '>', '/', '?', '\\',
        '`', '~'
    )

    fun isIllegalPassword(password: String): Boolean {
        return isShorterThanMinimum(password) ||
                isNotContainAlphabetic(password) ||
                isNotContainDigit(password) ||
                isNotContainSpecialCharacter(password) ||
                isEmailFormat(password) ||
                hasSequentialCharacters(password)
    }

    private fun isShorterThanMinimum(password: String): Boolean =
        password.length < MINIMUM_LENGTH

    private fun isNotContainAlphabetic(password: String): Boolean =
        password.none { it.isLetter() }

    private fun isNotContainDigit(password: String): Boolean =
        password.none { it.isDigit() }

    private fun isNotContainSpecialCharacter(password: String): Boolean =
        password.none { it in specialChars }

    private fun isEmailFormat(password: String): Boolean =
        "@" in password

    private fun hasSequentialCharacters(password: String): Boolean {
        val lower = password.lowercase()
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
}