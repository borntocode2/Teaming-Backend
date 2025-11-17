package goodspace.teaming.global.entity.room

import goodspace.teaming.global.entity.BaseEntity
import goodspace.teaming.global.entity.file.Attachment
import goodspace.teaming.global.entity.user.User
import jakarta.persistence.*
import jakarta.persistence.CascadeType.*
import jakarta.persistence.FetchType.*
import jakarta.persistence.GenerationType.IDENTITY
import org.hibernate.annotations.NotFound
import org.hibernate.annotations.NotFoundAction
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_chat_msg_room_sender_client",
            columnNames = ["room_id", "sender_id", "client_message_id"]
        )
    ]
)
@SQLDelete(sql = "UPDATE message SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
class Message(
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "sender_id")
    @NotFound(action = NotFoundAction.IGNORE) // 회원이 탈퇴해도 메시지는 남아야 하므로 null로 처리하도록 지정
    val sender: User?,

    @ManyToOne(fetch = LAZY)
    @JoinColumn(nullable = false)
    val room: Room,

    @Column(columnDefinition = "TEXT")
    val content: String?,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val type: MessageType = MessageType.TEXT,

    @Column(nullable = false)
    val clientMessageId: String
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    val id: Long? = null

    @OneToMany(fetch = LAZY, mappedBy = "message", cascade = [ALL])
    val attachments: MutableList<Attachment> = mutableListOf()

    @PrePersist
    @PreUpdate
    fun validate() {
        require(!(content.isNullOrBlank() && attachments.isEmpty())) {
            "본문과 첨부가 모두 비어있습니다."
        }
    }
}
