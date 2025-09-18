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
    @Value("\${admin.email1:test1@test.com}")
    private val email1: String,
    @Value("\${admin.password1:test1}")
    private val password1: String,
    @Value("\${admin.name1:Teaming Admin A}")
    private val name1: String,
    @Value("\${admin.email2:test2@test.com}")
    private val email2: String,
    @Value("\${admin.password2:test2}")
    private val password2: String,
    @Value("\${admin.name2:Teaming Admin B}")
    private val name2: String
) {
    @Transactional
    @EventListener(ApplicationReadyEvent::class)
    fun initialize() {
        val adminUser1 = createAdminUser(email1, password1, name1)
        val adminUser2 = createAdminUser(email2, password2, name2)

        saveIfNotExist(adminUser1)
        saveIfNotExist(adminUser2)
    }

    private fun createAdminUser(
        email: String,
        password: String,
        name: String
    ): User {
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

        return user
    }

    private fun saveIfNotExist(user: User) {
        if (!userRepository.existsByEmail(user.email)) {
            userRepository.save(user)
        }
    }
}
