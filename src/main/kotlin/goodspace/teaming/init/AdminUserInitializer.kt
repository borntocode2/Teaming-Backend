package goodspace.teaming.init

import goodspace.teaming.global.entity.user.Role
import goodspace.teaming.global.entity.user.TeamingUser
import goodspace.teaming.global.entity.user.User
import goodspace.teaming.global.entity.user.UserRole
import goodspace.teaming.global.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AdminUserInitializer(
    private val userRepository: UserRepository,
    @Value("\${admin.email:test@test.com}")
    private val email: String,
    @Value("\${admin.password:test}")
    private val password: String,
    @Value("\${admin.name:Teaming Admin}")
    private val name: String
) {
    @Transactional
    @EventListener(ApplicationReadyEvent::class)
    fun initialize() {
        val user = TeamingUser(
            email = email,
            password = password,
            name = name
        )

        val userRole = UserRole(
            user = user,
            role = Role.USER
        )
        val adminRole = UserRole(
            user = user,
            role = Role.ADMIN
        )
        user.addRole(userRole, adminRole)

        saveIfNotExist(user)
    }

    private fun saveIfNotExist(user: User) {
        if (!userRepository.existsByEmail(user.email)) {
            userRepository.save(user)
        }
    }
}
