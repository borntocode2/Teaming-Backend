package goodspace.teaming.global.entity.user

import goodspace.teaming.global.entity.BaseEntity
import goodspace.teaming.global.security.RefreshToken
import jakarta.persistence.*
import jakarta.persistence.EnumType.*
import jakarta.persistence.GenerationType.*
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction

@Entity
@Table(name = "`user`")
@Inheritance(strategy = InheritanceType.JOINED)
@SQLDelete(sql = "UPDATE `user` SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
abstract class User(
    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false)
    var name: String,

    var avatarKey: String? = null,

    var avatarVersion: Int? = null,

    @Enumerated(STRING)
    @Column(nullable = false)
    var type: UserType
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    val id: Long? = null

    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    private val refreshToken: RefreshToken = RefreshToken()

    var token = refreshToken.tokenValue
        set(value) {
            field = value
            refreshToken.tokenValue = value
        }
}
