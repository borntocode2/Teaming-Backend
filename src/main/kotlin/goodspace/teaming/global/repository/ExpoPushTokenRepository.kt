package goodspace.teaming.global.repository

import goodspace.teaming.global.entity.user.ExpoPushToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface ExpoPushTokenRepository : JpaRepository<ExpoPushToken, Long> {
    fun findByValue(value: String): ExpoPushToken?

    @Query("SELECT t FROM ExpoPushToken t WHERE t.user.id IN :userIds")
    fun findAllByUserIds(@Param("userIds") userIds: List<Long>): List<ExpoPushToken>

    fun deleteByValue(value: String)

    fun deleteAllByLastUsedAtBefore(cutoffTime: Instant)
}
