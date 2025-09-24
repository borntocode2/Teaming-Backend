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
    @Scheduled(fixedRate = 3600000) // 60분마다 실행
    fun checkAssignments() {
        val now = Instant.now()
        val expiredAssignments = assignmentRepository
            .findByDueBeforeAndStatusNotAndPunishedFalse(now, AssignmentStatus.COMPLETE)

        expiredAssignments.forEach { assignment ->
            punishmentService.applyPunishment(assignment)
        }
    }
}