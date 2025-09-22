package goodspace.teaming.global.password

interface PasswordValidator {
    fun isIllegalPassword(password: String): Boolean
}
