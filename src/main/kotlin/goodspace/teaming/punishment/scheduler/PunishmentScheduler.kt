package goodspace.teaming.punishment.scheduler

import goodspace.teaming.assignment.repository.AssignmentRepository
import goodspace.teaming.global.entity.aissgnment.AssignmentStatus
import goodspace.teaming.punishment.service.PunishmentService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class PunishmentScheduler (
    private val assignmentRepository: AssignmentRepository,
    private val punishmentService: PunishmentService
) {
    private val log = LoggerFactory.getLogger(PunishmentScheduler::class.java)

    @Scheduled(fixedRate = 3600000)
    @Transactional
    fun checkAssignments() {
        log.info("PunishmentScheduler 실행됨 at ${Instant.now()}")
        val now = Instant.now()
        log.info("Scheduler now = $now")
        val expiredAssignments = assignmentRepository
            .findByDueBeforeAndStatusNotAndPunishedFalse(now, AssignmentStatus.COMPLETE)

        log.info("expiredAssignments.size = ${expiredAssignments.size}")

        expiredAssignments.forEach { assignment ->
            punishmentService.applyPunishment(assignment)
        }
    }
}
