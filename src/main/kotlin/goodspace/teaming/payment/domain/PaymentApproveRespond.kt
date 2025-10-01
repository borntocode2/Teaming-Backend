package goodspace.teaming.payment.domain

import goodspace.teaming.global.entity.BaseEntity
import goodspace.teaming.global.entity.room.Room
import goodspace.teaming.global.entity.room.UserRoom
import goodspace.teaming.global.entity.user.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "payment_approve_respond")
class PaymentApproveRespond(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_room_id")
    var userRoom: UserRoom?,


    val resultCode: String,
    val resultMsg: String,

    @Column(unique = true)
    val tid: String,

    val cancelledTid: String? = null,

    @Column(unique = true)
    val orderId: String,

    val ediDate: LocalDateTime? = null,
    val signature: String,
    val status: String,
    val paidAt: LocalDateTime? = null,
    val failedAt: LocalDateTime? = null,
    val cancelledAt: LocalDateTime? = null,
    val payMethod: String,
    val amount: Int,
    val balanceAmt: Int,
    val goodsName: String,
    val mallReserved: String? = null,
    val useEscrow: Boolean,
    val currency: String,
    val channel: String,
    val approveNo: String,
    val buyerName: String? = null,
    val buyerTel: String? = null,
    val buyerEmail: String? = null,
    val receiptUrl: String,
    val mallUserId: String? = null,
    val issuedCashReceipt: Boolean

) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    // 카드 정보
    @Embedded
    var card: CardInfo? = null
}

@Embeddable
class CardInfo(
    val cardCode: String,
    val cardName: String,
    val cardNum: String,
    val cardQuota: Int,
    val isInterestFree: Boolean,
    val cardType: String,
    val canPartCancel: Boolean,
    val acquCardCode: String,
    val acquCardName: String
)
