package goodspace.teaming.payment.service

import goodspace.teaming.global.entity.room.PaymentStatus
import goodspace.teaming.global.entity.room.UserRoom
import goodspace.teaming.global.repository.UserRepository
import goodspace.teaming.global.repository.UserRoomRepository
import org.springframework.web.reactive.function.client.WebClient
import goodspace.teaming.payment.config.NicepayProperties
import goodspace.teaming.payment.domain.PaymentApproveRespond
import goodspace.teaming.payment.dto.PaymentApproveRequestDto
import goodspace.teaming.payment.dto.PaymentApproveRespondDto
import goodspace.teaming.payment.dto.PaymentVerifyRespondDto
import goodspace.teaming.payment.dto.toEntity
import goodspace.teaming.payment.repository.PaymentRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class PaymentService(
    private val nicepayProperties: NicepayProperties,
    private val webClient: WebClient.Builder,
    private val paymentRepository: PaymentRepository,
    private val userRoomRepository: UserRoomRepository
){
    @Transactional
    fun requestApprove(paymentVerifyRespondDto: PaymentVerifyRespondDto): ResponseEntity<Void> {
        if (paymentVerifyRespondDto.authResultCode != "0000"){
            throw RuntimeException("카드사인증 인증 실패: ${paymentVerifyRespondDto.authResultCode} in requestApprove Service Layer")
        }

        val amount = paymentVerifyRespondDto.amount
        val (userId, roomId, platform) = paymentVerifyRespondDto.mallReserved.split(":")

        val paymentApproveRespondDto = approvePayment(paymentVerifyRespondDto.tid, amount, userId, roomId, platform)

        val redirectUrl = when {
            paymentApproveRespondDto.resultCode == "0000" && platform == "APP" -> "teaming://payment/success"
            paymentApproveRespondDto.resultCode != "0000" && platform == "APP" -> "teaming://payment/fail"
            paymentApproveRespondDto.resultCode == "0000" && platform == "WEB" -> "https://teaming-three.vercel.app/payment/success"
            else -> "https://teaming-three.vercel.app/payment/fail"
        }

        if (paymentApproveRespondDto.resultCode == "0000") {
            savePaymentResult(paymentApproveRespondDto.toEntity())
        }

        return ResponseEntity.status(HttpStatus.FOUND)
            .header("Location", redirectUrl)
            .build()
    }

    @Transactional
    fun approvePayment(tid: String, amount: String, userId: String, roomId: String, platform: String): PaymentApproveRespondDto {
        val url = "${nicepayProperties.approveUrl}/$tid"
        val request = PaymentApproveRequestDto(
            amount = amount
        )

        val paymentApproveRespondDto = webClient.build()
            .post()
            .uri(url)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, getAuthHeader())
            .bodyValue(request)
            .retrieve()
            .bodyToMono(PaymentApproveRespondDto::class.java)
            .block() ?: throw RuntimeException("결제 승인 응답이 올바르지 않습니다.")


        if (paymentApproveRespondDto.resultCode == "0000" || paymentApproveRespondDto.resultCode == "3001") {
            saveUserRoomInfo(userId, roomId)
        }

        return paymentApproveRespondDto
    }

    @Transactional
    fun savePaymentResult(paymentApproveRespond: PaymentApproveRespond): String {
        paymentRepository.save(paymentApproveRespond)
        return paymentApproveRespond.tid
    }
    @Transactional
    fun saveUserRoomInfo(userId: String, roomId: String){
       val userRoom = userRoomRepository.findByRoomIdAndUserId(roomId.toLong(), userId.toLong())
           ?: throw IllegalArgumentException("결제를 요청한 유저의 방을 찾을 수 없습니다.")

        userRoom.paymentStatus = PaymentStatus.PAID
        userRoomRepository.save(userRoom)
    }

    private fun getAuthHeader(): String{
        val credentials = "${nicepayProperties.clientId}:${nicepayProperties.secretKey}"
        val encoded = Base64.getEncoder().encodeToString(credentials.toByteArray())

        return "Basic $encoded"
    }

    fun  mapResultCodeToHttpStatus(resultCode: String): ResponseEntity<HttpStatus> {
        if (resultCode == "0000" || resultCode == "3001") {
            return ResponseEntity(HttpStatus.OK)
        }
        else{
            return ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    fun generateUUIDString(): String {
        return UUID.randomUUID().toString().take(10)
    }
}
