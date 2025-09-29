package goodspace.teaming.punishment.scheduler

import goodspace.teaming.assignment.repository.AssignmentRepository
import goodspace.teaming.global.entity.aissgnment.Assignment
import goodspace.teaming.global.entity.aissgnment.AssignmentStatus
import goodspace.teaming.punishment.service.PunishmentService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class PunishmentScheduler (
    private val assignmentRepository: AssignmentRepository,
    private val punishmentService: PunishmentService
) {
    @Scheduled(fixedRate = 1000) //TODO: 60분마다 실행
    @Transactional
    fun checkAssignments() {
        println("PunishmentScheduler 실행됨 at ${Instant.now()}")
        val now = Instant.now()
        println("Scheduler now = $now")
        val expiredAssignments = assignmentRepository
            .findByDueBeforeAndStatusNotAndPunishedFalse(now, AssignmentStatus.COMPLETE)

        println("expiredAssignments.size = ${expiredAssignments.size}")

        expiredAssignments.forEach { assignment ->
            punishmentService.applyPunishment(assignment)
        }
    }
}
