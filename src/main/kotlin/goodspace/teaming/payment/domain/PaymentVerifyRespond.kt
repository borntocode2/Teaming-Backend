package goodspace.teaming.payment.domain

import goodspace.teaming.global.entity.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class PaymentVerifyRespond(
    val authResultCode: String,
    val tid: String,
    val orderId: String,
    val amount: String
    ) : BaseEntity() {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long?= null

}