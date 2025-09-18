package goodspace.teaming.global.entity.aissgnment

import goodspace.teaming.global.entity.BaseEntity
import jakarta.persistence.*
import jakarta.persistence.CascadeType.*
import jakarta.persistence.FetchType.*
import jakarta.persistence.GenerationType.*
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

    @Column(nullable = false)
    val submitterId: Long,

    @Column(nullable = false)
    val description: String,
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    val id: Long? = null

    @OneToMany(mappedBy = "submission", fetch = LAZY, cascade = [ALL], orphanRemoval = true)
    val submittedFiles: MutableList<SubmittedFile> = mutableListOf()

    val files
        get() = this.submittedFiles.map { it.file }

    fun addSubmittedFiles(submittedFiles: List<SubmittedFile>) {
        this.submittedFiles.addAll(submittedFiles)
    }
}
