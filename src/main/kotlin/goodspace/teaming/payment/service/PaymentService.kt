package goodspace.teaming.payment.service

import goodspace.teaming.global.entity.room.PaymentStatus
import goodspace.teaming.global.repository.RoomRepository
import goodspace.teaming.global.repository.UserRepository
import goodspace.teaming.global.repository.UserRoomRepository
import org.springframework.web.reactive.function.client.WebClient
import goodspace.teaming.payment.config.NicepayProperties
import goodspace.teaming.payment.domain.PaymentApproveRespond
import goodspace.teaming.payment.dto.*

import goodspace.teaming.payment.repository.PaymentRepository
import jakarta.persistence.EntityNotFoundException
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
    private val roomRepository: RoomRepository,
    private val userRepository: UserRepository,
    private val userRoomRepository: UserRoomRepository
){
    @Transactional
    fun requestApprove(paymentVerifyRespondDto: PaymentVerifyRespondDto): ResponseEntity<Void> {
        if (paymentVerifyRespondDto.authResultCode != "0000") {
            val platform = paymentVerifyRespondDto.mallReserved.split(":")[2]

            //TODO: 쿼리스트링으로 반환
            val redirectUrl = if (platform == "APP") "teaming://payment/fail"
            else "https://teaming-three.vercel.app/payment/fail"

            return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", redirectUrl)
                .build()
        }

        val amount = paymentVerifyRespondDto.amount
        val (userId, roomId, platform) = paymentVerifyRespondDto.mallReserved.split(":")

        val paymentApproveRespondDto = approvePayment(paymentVerifyRespondDto.tid, amount, userId, roomId, platform)

        //TODO: 쿼리스트링으로 반환
        val redirectUrl = when {
            paymentApproveRespondDto.resultCode == "0000" && platform == "APP" -> "teaming://payment/success"
            paymentApproveRespondDto.resultCode != "0000" && platform == "APP" -> "teaming://payment/fail"
            paymentApproveRespondDto.resultCode == "0000" && platform == "WEB" -> "https://teaming-three.vercel.app/payment/success"
            else -> "https://teaming-three.vercel.app/payment/fail"
        }

        if (paymentApproveRespondDto.resultCode == "0000") {
            savePaymentResult(paymentApproveRespondDto.toEntity(), userId.toLong(), roomId.toLong())

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
    fun requestCancel(roomId: Long): ResponseEntity<Void> {
        val room = roomRepository.findById(roomId).orElseThrow { EntityNotFoundException("Room not found") }

        val userRoomsToCancel = room.userRooms.filter{( !it.isPunished) }

        userRoomsToCancel.forEach { userRoom ->
            val user = userRoom.user

            val payment = paymentRepository.findByUserAndRoom(user, room)
                ?: throw EntityNotFoundException("유저(id:${user.id})의 결제 정보를 찾을 수 없습니다.")

            if (payment.status != "CANCELLED") {
                approveCancel(payment.tid, payment.amount.toString())
            }
        }
        // 위에서 오류가 나는 경우는 이미다 예외처리를 해놨으니 return에서 어떤 상태를 반환할 필요가 없다.?
        // TODO: 준이 체크
        return ResponseEntity.ok().build()
    }

    @Transactional
    fun approveCancel(tid: String, amount: String): PaymentCancelResponseDto {
        val url = "${nicepayProperties.approveUrl}/$tid/cancel"
        val requestBody = PaymentCancelRequestDto(
            amount = amount,
            reason = "미션 성공으로 인한 미벌칙자 환급"
        )

        val cancelResponseDto = webClient.build()
            .post()
            .uri(url)
            .header(HttpHeaders.AUTHORIZATION, getAuthHeader())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(PaymentCancelResponseDto::class.java)
            .block() ?: throw IllegalStateException("결제 취소 응답이 올바르지 않습니다.")

        if(cancelResponseDto.resultCode == "0000") {
            var paymentApproveRespond = paymentRepository.findByTid(tid) ?: throw EntityNotFoundException("결제 취소 로직 중에 해당 TID에 해당하는 결제 정보를 찾을 수 없습니다.")
            paymentApproveRespond.status = "CANCELLED"
        }

        return cancelResponseDto
    }

    @Transactional
    fun savePaymentResult(paymentApproveRespond: PaymentApproveRespond, userId: Long, roomId: Long): String {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("User not found with id $userId") }
        val room = roomRepository.findById(roomId).orElseThrow { IllegalArgumentException("Room not found with id $roomId") }

        paymentApproveRespond.room = room
        paymentApproveRespond.user = user
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
        val credentials = "${nicepayProperties.clientIdOperation}:${nicepayProperties.secretKeyOperation}"
        val encoded = Base64.getEncoder().encodeToString(credentials.toByteArray())

        return "Basic $encoded"
    }

    fun generateUUIDString(): String {
        return UUID.randomUUID().toString().take(10)
    }
}
