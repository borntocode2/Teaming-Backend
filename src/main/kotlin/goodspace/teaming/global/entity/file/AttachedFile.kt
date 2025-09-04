package goodspace.teaming.global.entity.file

import goodspace.teaming.global.entity.room.Message
import goodspace.teaming.global.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType.IDENTITY
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction

@Entity
@SQLDelete(sql = "UPDATE attached_file SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
class AttachedFile(
    @ManyToOne
    @JoinColumn(nullable = false)
    val message: Message,

    @ManyToOne
    @JoinColumn(nullable = false)
    val file: File,

    @Column(nullable = false)
    val sortOrder: Int = 0
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    val id: Long? = null
}
