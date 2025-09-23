package goodspace.teaming.assignment.repository

import goodspace.teaming.global.entity.aissgnment.Assignment
import goodspace.teaming.global.entity.aissgnment.AssignmentStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant


interface AssignmentRepository : JpaRepository<Assignment, Long> {
    fun findByDueBeforeAndStatusNotAndPunishedFalse(
        now: Instant,
        status: AssignmentStatus,
    ): List<Assignment>
}