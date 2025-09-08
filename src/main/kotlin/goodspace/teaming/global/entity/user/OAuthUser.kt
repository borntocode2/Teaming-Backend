package goodspace.teaming.global.entity.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import org.hibernate.annotations.SQLDelete

@Entity
@SQLDelete(
    sql = "UPDATE teaming_user " +
            "SET password = CONCAT('DELETED_', password) " +
            "WHERE id = ?"
)
class OAuthUser(
    @Column(nullable = false)
    val identifier: String,

    email: String,
    name: String,
    avatarKey: String? = null,
    avatarVersion: Int? = null,
    type: UserType
) : User(
    email = email,
    name = name,
    avatarKey = avatarKey,
    avatarVersion = avatarVersion,
    type = type
)
