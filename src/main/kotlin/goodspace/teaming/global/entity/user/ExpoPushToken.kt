package goodspace.teaming.global.entity.user

import jakarta.persistence.*
import java.time.Instant

@Entity
class ExpoPushToken(
    @Column(unique = true)
    val value: String,

    @ManyToOne
    var user: User
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    var lastRegisteredAt: Instant = Instant.now()
}
