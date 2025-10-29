package goodspace.teaming.init

import goodspace.teaming.global.entity.user.Role
import goodspace.teaming.global.entity.user.TeamingUser
import goodspace.teaming.global.entity.user.User
import goodspace.teaming.global.entity.user.UserRole
import goodspace.teaming.global.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AdminUserInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,

    // 1~10번까지 기본값 자동 세팅
    @Value("\${admin.email1:test1@test.com}") private val email1: String,
    @Value("\${admin.password1:test1}") private val password1: String,
    @Value("\${admin.name1:Teaming Admin A}") private val name1: String,

    @Value("\${admin.email2:test2@test.com}") private val email2: String,
    @Value("\${admin.password2:test2}") private val password2: String,
    @Value("\${admin.name2:Teaming Admin B}") private val name2: String,

    @Value("\${admin.email3:test3@test.com}") private val email3: String,
    @Value("\${admin.password3:test3}") private val password3: String,
    @Value("\${admin.name3:Teaming Admin C}") private val name3: String,

    @Value("\${admin.email4:test4@test.com}") private val email4: String,
    @Value("\${admin.password4:test4}") private val password4: String,
    @Value("\${admin.name4:Teaming Admin D}") private val name4: String,

    @Value("\${admin.email5:test5@test.com}") private val email5: String,
    @Value("\${admin.password5:test5}") private val password5: String,
    @Value("\${admin.name5:Teaming Admin E}") private val name5: String,

    @Value("\${admin.email6:test6@test.com}") private val email6: String,
    @Value("\${admin.password6:test6}") private val password6: String,
    @Value("\${admin.name6:Teaming Admin F}") private val name6: String,

    @Value("\${admin.email7:test7@test.com}") private val email7: String,
    @Value("\${admin.password7:test7}") private val password7: String,
    @Value("\${admin.name7:Teaming Admin G}") private val name7: String,

    @Value("\${admin.email8:test8@test.com}") private val email8: String,
    @Value("\${admin.password8:test8}") private val password8: String,
    @Value("\${admin.name8:Teaming Admin H}") private val name8: String,

    @Value("\${admin.email9:test9@test.com}") private val email9: String,
    @Value("\${admin.password9:test9}") private val password9: String,
    @Value("\${admin.name9:Teaming Admin I}") private val name9: String,

    @Value("\${admin.email10:test10@test.com}") private val email10: String,
    @Value("\${admin.password10:test10}") private val password10: String,
    @Value("\${admin.name10:Teaming Admin J}") private val name10: String
) {
    @Transactional
    @EventListener(ApplicationReadyEvent::class)
    fun initialize() {
        val adminInfos = listOf(
            Triple(email1, password1, name1),
            Triple(email2, password2, name2),
            Triple(email3, password3, name3),
            Triple(email4, password4, name4),
            Triple(email5, password5, name5),
            Triple(email6, password6, name6),
            Triple(email7, password7, name7),
            Triple(email8, password8, name8),
            Triple(email9, password9, name9),
            Triple(email10, password10, name10)
        )

        adminInfos
            .filter { it.first.isNotBlank() && it.second.isNotBlank() && it.third.isNotBlank() }
            .map { createAdminUser(it.first, it.second, it.third) }
            .forEach { saveIfNotExist(it) }
    }

    private fun createAdminUser(email: String, password: String, name: String): User {
        val user = TeamingUser(
            email = email,
            password = passwordEncoder.encode(password),
            name = name
        )

        val userRole = UserRole(user = user, role = Role.USER)
        val adminRole = UserRole(user = user, role = Role.ADMIN)
        user.addRole(userRole, adminRole)

        return user
    }

    private fun saveIfNotExist(user: User) {
        if (!userRepository.existsByEmail(user.email)) {
            userRepository.save(user)
        }
    }
}
