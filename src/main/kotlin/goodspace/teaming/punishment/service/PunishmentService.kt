package goodspace.teaming.punishment.service

import goodspace.teaming.assignment.repository.AssignmentRepository
import goodspace.teaming.gifticon.service.GifticonService
import goodspace.teaming.global.entity.aissgnment.Assignment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PunishmentService(
    private val gifticonService: GifticonService,
    private val assignmentRepository: AssignmentRepository
) {
    @Transactional
    fun applyPunishment(assignment: Assignment) {
        val room = assignment.room
        val users = room.userRooms.map { it.user }

        users.forEach { user ->
            gifticonService.sendGifticon(user)
        }

        assignment.punished = true

        assignmentRepository.save(assignment)
    }
}