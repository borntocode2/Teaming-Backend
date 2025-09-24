package goodspace.teaming.global.repository

import goodspace.teaming.global.entity.user.OAuthUser
import goodspace.teaming.global.entity.user.TeamingUser
import goodspace.teaming.global.entity.user.User
import goodspace.teaming.global.entity.user.UserType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserRepository : JpaRepository<User, Long> {
    fun existsByEmail(email: String): Boolean

    @Query("SELECT u FROM OAuthUser u WHERE u.identifier = :identifier AND u.type = :userType")
    fun findByIdentifierAndUserType(
        @Param("identifier") identifier: String,
        @Param("userType") userType: UserType
    ): OAuthUser?

    @Query("SELECT u FROM TeamingUser u WHERE u.id = :id")
    fun findTeamingUserById(id: Long): TeamingUser?

    @Query("SELECT u FROM TeamingUser u WHERE u.email = :email")
    fun findTeamingUserByEmail(email: String): TeamingUser?
}
