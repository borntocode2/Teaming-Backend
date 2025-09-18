package goodspace.teaming.global.entity.aissgnment

import goodspace.teaming.global.entity.BaseEntity
import goodspace.teaming.global.entity.file.File
import jakarta.persistence.*
import jakarta.persistence.CascadeType.*
import jakarta.persistence.FetchType.*
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@SQLDelete(sql = "UPDATE `submission` SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
class Submission(
    @ManyToOne(fetch = LAZY)
    @JoinColumn(nullable = false)
    val assignment: Assignment,

    @OneToMany(fetch = LAZY, cascade = [ALL], orphanRemoval = true)
    val files: MutableList<File> = mutableListOf(),

    @Column(nullable = false)
    val submitterId: Long,

    @Column(nullable = false)
    val description: String,
) : BaseEntity() {
    @Id
    @GeneratedValue
    val id: Long? = null
}
