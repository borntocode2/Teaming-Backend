package goodspace.teaming.payment.repository

import goodspace.teaming.global.entity.room.Room
import goodspace.teaming.global.entity.user.User
import goodspace.teaming.payment.domain.PaymentApproveRespond
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentRepository: JpaRepository<PaymentApproveRespond, Long> {
    fun findByTid(tid: String): PaymentApproveRespond?
    fun findByUserAndRoom(user: User, room: Room): PaymentApproveRespond?
}