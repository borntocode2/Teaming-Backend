package goodspace.teaming.global.entity.email

import goodspace.teaming.global.entity.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDateTime

/**
 * 이메일 인증 코드 저장용 엔티티
 */
@Entity
@SQLDelete(sql = "UPDATE email_verification SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
class EmailVerification(
    /** 인증 대상 이메일 */
    @Column(unique = true, nullable = false, length = 254)
    val email: String,

    /** 발급된 인증 코드 */
    @Column(nullable = false, length = 20)
    var code: String,

    /** 인증 코드 만료 시각 */
    @Column(nullable = false)
    var expiresAt: LocalDateTime,

    /** 인증됨 여부 */
    @Column(nullable = false)
    var verified: Boolean = false
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    fun hasSameCode(code: String): Boolean = this.code == code

    fun isNotExpired(referenceTime: LocalDateTime): Boolean = expiresAt.isAfter(referenceTime)

    fun verify() {
        verified = true
    }
}
