package goodspace.teaming.push.scheduler

import goodspace.teaming.global.repository.ExpoPushTokenRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class TokenCleanScheduler(
    @Value("\${push.token.threshold.days}:90")
    private val inactivityThresholdDays: Long,

    private val tokenRepository: ExpoPushTokenRepository
) {
    private val logger = KotlinLogging.logger { }

    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    @Transactional
    fun cleanupOldTokens() {
        val cutoff = Instant.now().minus(inactivityThresholdDays, ChronoUnit.DAYS)

        logger.info { "오래된 푸시 토큰 정리 작업 시작 (갱신일이 ${inactivityThresholdDays}일 보다 이전인 [$cutoff] 토큰 삭제)" }

        try {
            tokenRepository.deleteAllByLastUsedAtBefore(cutoff)
            logger.info { "오래된 푸시 토큰 정리 작업 완료." }
        } catch (e: Exception) {
            logger.error(e) { "오래된 푸시 토큰 정리 작업 중 오류 발생" }
        }
    }
}
