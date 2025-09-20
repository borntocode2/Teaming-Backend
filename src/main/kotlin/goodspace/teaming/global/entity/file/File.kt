package goodspace.teaming.global.entity.file

import goodspace.teaming.global.entity.BaseEntity
import goodspace.teaming.global.entity.file.AntiVirusScanStatus.PASSED
import goodspace.teaming.global.entity.file.TranscodeStatus.NONE
import goodspace.teaming.global.entity.room.Room
import jakarta.persistence.*
import jakarta.persistence.EnumType.STRING
import jakarta.persistence.GenerationType.IDENTITY
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction

@Entity
@SQLDelete(sql = "UPDATE file SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
class File(
    @ManyToOne
    @JoinColumn(nullable = false)
    val room: Room,

    @Column(nullable = false)
    val uploaderId: Long,

    @Column(nullable = false)
    var name: String,

    @Enumerated(STRING)
    @Column(nullable = false)
    var type: FileType,

    @Column(nullable = false)
    var mimeType: String,

    @Column(nullable = false)
    var byteSize: Long,

    @Column(nullable = false)
    var storageKey: String,

    @Column(nullable = false)
    var storageBucket: String,

    @Column(nullable = false)
    var checksumSha256: String,

    var width: Int? = null,

    var height: Int? = null,

    var durationMs: Int? = null,

    var thumbnailKey: String? = null,

    @Enumerated(STRING)
    @Column(nullable = false)
    var antiVirusScanStatus: AntiVirusScanStatus = PASSED,

    @Enumerated(STRING)
    @Column(nullable = false)
    var transcodeStatus: TranscodeStatus = NONE
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    val id: Long? = null
}

