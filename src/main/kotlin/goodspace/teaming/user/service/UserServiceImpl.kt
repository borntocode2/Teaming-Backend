package goodspace.teaming.user.service

import goodspace.teaming.global.entity.user.TeamingUser
import goodspace.teaming.global.entity.user.User
import goodspace.teaming.global.entity.user.UserType
import goodspace.teaming.global.password.PasswordValidator
import goodspace.teaming.global.repository.EmailVerificationRepository
import goodspace.teaming.global.repository.UserRepository
import goodspace.teaming.user.domain.mapper.UserInfoMapper
import goodspace.teaming.user.dto.*
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private const val USER_NOT_FOUND = "회원을 조회할 수 없습니다."
private const val ILLEGAL_USER_TYPE = "소셜 회원은 이메일을 변경할 수 없습니다."
private const val EMAIL_ALREADY_EXISTS = "이미 사용 중인 이메일입니다."
private const val EMAIL_NOT_VERIFIED = "인증되지 않은 이메일입니다."
private const val WRONG_PASSWORD = "비밀번호가 올바르지 않습니다."
private const val ILLEGAL_PASSWORD = "부적절한 비밀번호입니다."

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val emailVerificationRepository: EmailVerificationRepository,
    private val passwordEncoder: PasswordEncoder,
    private val passwordValidator: PasswordValidator,
    private val userInfoMapper: UserInfoMapper
) : UserService {
    @Transactional(readOnly = true)
    override fun getUserInfo(userId: Long): UserInfoResponseDto {
        val user = findUser(userId)

        return userInfoMapper.map(user)
    }

    @Transactional
    override fun updateEmail(userId: Long, requestDto: UpdateEmailRequestDto) {
        val user = findUser(userId)
        val email = requestDto.email

        checkUserType(user)
        checkEmailAlreadyExists(email)
        checkEmailVerification(email)

        user.email = email
    }

    @Transactional
    override fun updatePassword(userId: Long, requestDto: UpdatePasswordRequestDto) {
        checkEmailVerification(requestDto.email)

        val user = findTeamingUser(userId)
        val actualCurrentPassword = user.password
        val expectedCurrentPassword = passwordEncoder.encode(requestDto.currentPassword)

        assertEqualWithCurrentPassword(actualCurrentPassword, expectedCurrentPassword)

        val newPassword = requestDto.newPassword
        assertProperPassword(newPassword)

        user.password = passwordEncoder.encode(newPassword)
    }

    @Transactional
    override fun updateName(userId: Long, requestDto: UpdateNameRequestDto) {
        val user = findUser(userId)

        user.name = requestDto.name
    }

    @Transactional
    override fun removeUser(userId: Long) {
        userRepository.deleteById(userId)
    }

    private fun findUser(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException(USER_NOT_FOUND) }
    }

    private fun findTeamingUser(userId: Long): TeamingUser {
        return userRepository.findTeamingUserById(userId)
            ?: throw IllegalArgumentException(USER_NOT_FOUND)
    }

    private fun checkUserType(user: User) {
        check(user.type == UserType.TEAMING) { ILLEGAL_USER_TYPE }
    }

    private fun checkEmailAlreadyExists(email: String) {
        require(!userRepository.existsByEmail(email)) { EMAIL_ALREADY_EXISTS }
    }

    private fun checkEmailVerification(email: String) {
        val emailVerification = emailVerificationRepository.findByEmail(email)
            ?: throw IllegalStateException(EMAIL_NOT_VERIFIED)

        check(emailVerification.verified) { EMAIL_NOT_VERIFIED }

        emailVerificationRepository.delete(emailVerification)
    }

    private fun assertEqualWithCurrentPassword(actualCurrentPassword: String, expectedCurrentPassword: String) {
        require(actualCurrentPassword == expectedCurrentPassword) { WRONG_PASSWORD }
    }

    private fun assertProperPassword(password: String) {
        require(!passwordValidator.isIllegalPassword(password)) { ILLEGAL_PASSWORD }
    }
}
