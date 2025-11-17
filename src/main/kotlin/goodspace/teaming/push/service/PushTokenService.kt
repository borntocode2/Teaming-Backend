package goodspace.teaming.push.service

import goodspace.teaming.global.entity.user.ExpoPushToken
import goodspace.teaming.global.entity.user.User
import goodspace.teaming.global.exception.USER_NOT_FOUND
import goodspace.teaming.global.repository.ExpoPushTokenRepository
import goodspace.teaming.global.repository.UserRepository
import goodspace.teaming.push.dto.PushTokenDeleteRequestDto
import goodspace.teaming.push.dto.PushTokenRegisterRequestDto
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class PushTokenService(
    private val userRepository: UserRepository,
    private val tokenRepository: ExpoPushTokenRepository
) {
    @Transactional
    fun registerOrUpdate(
        requestDto: PushTokenRegisterRequestDto,
        userId: Long
    ) {
        val value = requestDto.token
        val user = findUserBy(userId)

        tokenRepository.findByValue(value)
            ?.apply { updateExistingToken(this, user) }
            ?: registerNewToken(user, value)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun delete(
        tokenId: Long,
    ) {
        tokenRepository.deleteById(tokenId)
    }

    @Transactional
    fun delete(
        requestDto: PushTokenDeleteRequestDto
    ) {
        tokenRepository.deleteByValue(requestDto.token)
    }

    private fun findUserBy(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow { EntityNotFoundException(USER_NOT_FOUND) }
    }

    private fun registerNewToken(user: User, value: String) {
        val token = ExpoPushToken(
            user = user,
            value = value
        )

        tokenRepository.save(token)
    }

    private fun updateExistingToken(existingToken: ExpoPushToken, user: User) {
        existingToken.user = user
        existingToken.lastUsedAt = Instant.now()
    }
}
