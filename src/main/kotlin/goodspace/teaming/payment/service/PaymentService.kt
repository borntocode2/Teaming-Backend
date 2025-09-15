package goodspace.teaming.payment.service

import org.springframework.web.reactive.function.client.WebClient
import goodspace.teaming.payment.config.NicepayProperties
import goodspace.teaming.payment.domain.PaymentApproveRespond
import goodspace.teaming.payment.dto.PaymentApproveRequestDto
import goodspace.teaming.payment.dto.PaymentApproveRespondDto
import goodspace.teaming.payment.dto.PaymentVerifyRespondDto
import goodspace.teaming.payment.repository.PaymentRepository
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import java.util.*

@Service
class PaymentService(
    private val nicepayProperties: NicepayProperties,
    private val webClient: WebClient.Builder,
    private val paymentRepository: PaymentRepository
){

    fun requestApprove(paymentVerifyRespondDto: PaymentVerifyRespondDto): PaymentApproveRespondDto {
        if (paymentVerifyRespondDto.authResultCode != "0000"){
            throw RuntimeException("결제 인증 실패: ${paymentVerifyRespondDto.authResultCode} in requestApprove Service Layer")
        }

        val amount = paymentVerifyRespondDto.amount
        return approvePayment(paymentVerifyRespondDto.tid, amount)
    }

    private fun approvePayment(tid: String, amount: String): PaymentApproveRespondDto {
        val url = "${nicepayProperties.approveUrl}/$tid"
        val request = PaymentApproveRequestDto(
            amount = amount
        )

        return webClient.build()
            .post()
            .uri(url)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, getAuthHeader())
            .bodyValue(request)
            .retrieve()
            .bodyToMono(PaymentApproveRespondDto::class.java)
            .block() ?: throw RuntimeException("결제 승인 응답이 올바르지 않습니다.")
    }

    fun savePaymentResult(paymentApproveRespond: PaymentApproveRespond): String {
        paymentRepository.save(paymentApproveRespond)
        return paymentApproveRespond.tid
    }

    private fun getAuthHeader(): String{
        val credentials = "${nicepayProperties.clientId}:${nicepayProperties.secretKey}"
        val encoded = Base64.getEncoder().encodeToString(credentials.toByteArray())

        return "Basic $encoded"
    }
}
