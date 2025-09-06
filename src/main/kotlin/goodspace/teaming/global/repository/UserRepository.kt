package goodspace.teaming.global.repository

import goodspace.teaming.global.entity.user.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun existsByEmail(email: String): Boolean
}
