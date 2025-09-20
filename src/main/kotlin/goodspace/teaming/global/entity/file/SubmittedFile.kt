package goodspace.teaming.global.entity.file

import goodspace.teaming.global.entity.BaseEntity
import goodspace.teaming.global.entity.aissgnment.Submission
import jakarta.persistence.*
import jakarta.persistence.GenerationType.*
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@SQLDelete(sql = "UPDATE `submitted_file` SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
class SubmittedFile(
    @ManyToOne
    val submission: Submission,
    @OneToOne
    val file: File
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    val id: Long? = null
}
