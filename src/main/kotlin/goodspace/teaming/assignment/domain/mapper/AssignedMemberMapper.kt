package goodspace.teaming.assignment.domain.mapper

import goodspace.teaming.global.entity.aissgnment.AssignedMember
import goodspace.teaming.global.entity.aissgnment.Assignment
import goodspace.teaming.global.entity.room.Room
import goodspace.teaming.global.entity.user.User
import org.springframework.stereotype.Component

private const val NOT_MEMBER = "해당 티밍룸에 소속되어 있지 않습니다."

@Component
class AssignedMemberMapper {
    fun map(user: User, room: Room, assignment: Assignment): AssignedMember {
        val memberSet = room.memberSet()
        require(memberSet.contains(user)) { NOT_MEMBER }

        return AssignedMember(
            user = user,
            assignment = assignment
        )
    }

    private fun Room.memberSet(): Set<User> {
        return this.userRooms
            .map { it.user }
            .toSet()
    }
}
