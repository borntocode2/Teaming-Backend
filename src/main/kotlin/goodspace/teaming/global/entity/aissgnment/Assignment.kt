package goodspace.teaming.global.entity.aissgnment

import goodspace.teaming.global.entity.BaseEntity
import goodspace.teaming.global.entity.aissgnment.AssignmentStatus.IN_PROGRESS
import goodspace.teaming.global.entity.room.Room
import jakarta.persistence.*
import jakarta.persistence.CascadeType.ALL
import jakarta.persistence.EnumType.STRING
import jakarta.persistence.FetchType.LAZY
import jakarta.persistence.GenerationType.IDENTITY
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import java.time.Instant

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@SQLDelete(sql = "UPDATE `assignment` SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
class Assignment(
    @Column(nullable = false)
    val title: String,

    @Column(nullable = false)
    val description: String,

    @ManyToOne(fetch = LAZY)
    @JoinColumn(nullable = false)
    val room: Room,

    @OneToMany(mappedBy = "assignment", fetch = LAZY, cascade = [ALL])
    val submissions: MutableList<Submission> = mutableListOf(),

    @Column(nullable = false)
    val due: Instant,

    @Column(nullable = false)
    @Enumerated(STRING)
    var status: AssignmentStatus = IN_PROGRESS,
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    val id: Long? = null

    @OneToMany(mappedBy = "assignment", fetch = LAZY, cascade = [ALL])
    private val assignedMembers: MutableList<AssignedMember> = mutableListOf()

    val assignedMemberIds: List<Long>
        get() = assignedMembers.map { it.user.id!! }

    fun addAssignedMembers(assignedMembers: Collection<AssignedMember>) {
        this.assignedMembers.addAll(assignedMembers)
    }
    fun addAssignedMember(assignedMember: AssignedMember) {
        assignedMembers.add(assignedMember)
    }

    fun addSubmission(submission: Submission) {
        submissions.add(submission)
    }
}
