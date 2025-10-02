package goodspace.teaming.punishment.service

import goodspace.teaming.assignment.repository.AssignmentRepository
import goodspace.teaming.gifticon.service.GifticonService
import goodspace.teaming.global.entity.aissgnment.Assignment
import goodspace.teaming.global.entity.aissgnment.AssignmentStatus
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
        val roomType = assignment.room.type
        val users = room.userRooms.map { it.user }

        users.filter{ user -> user.id !in assignment.assignedMemberIds }
            .forEach { user ->
            gifticonService.sendGifticon(user, roomType)
        }

        room.userRooms
            .filter { userRoom -> userRoom.user.id in assignment.assignedMemberIds }
            .forEach { userRoom ->
                userRoom.isPunished = true
            }

        assignment.punished = true
        assignment.status = AssignmentStatus.COMPLETE

        assignmentRepository.save(assignment)
    }
}