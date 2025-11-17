package goodspace.teaming.global.repository

import goodspace.teaming.global.entity.user.ExpoPushToken
import org.springframework.data.jpa.repository.JpaRepository

interface ExpoPushTokenRepository : JpaRepository<ExpoPushToken, Long> {
    fun findByValue(value: String): ExpoPushToken?
}
