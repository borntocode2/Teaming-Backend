package goodspace.teaming.payment.controller

import goodspace.teaming.payment.dto.PaymentApproveRespondDto
import goodspace.teaming.payment.dto.PaymentVerifyRespondDto
import goodspace.teaming.payment.dto.toEntity
import goodspace.teaming.payment.service.PaymentService
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/payment")
class PaymentController(
    private val paymentService: PaymentService
){
    @PostMapping("/request")
    fun requestPayment(@RequestParam params: MultiValueMap<String, String>): ResponseEntity<String> {
        val dto = PaymentVerifyRespondDto(
            authResultCode = params["authResultCode"]?.firstOrNull() ?: "",
            authResultMsg = params["authResultMsg"]?.firstOrNull() ?: "",
            tid = params["tid"]?.firstOrNull() ?: "",
            clientId = params["clientId"]?.firstOrNull() ?: "",
            orderId = params["orderId"]?.firstOrNull() ?: "",
            amount = params["amount"]?.firstOrNull() ?: "",
            mallReserved = params["mallReserved"]?.firstOrNull() ?: "",
            authToken = params["authToken"]?.firstOrNull() ?: "",
            signature = params["signature"]?.firstOrNull() ?: ""
        )

        val paymentApproveRespondDto: PaymentApproveRespondDto = paymentService.requestApprove(dto)

        return ResponseEntity.ok("승인 완료, TID: ${paymentService.savePaymentResult(paymentApproveRespondDto.toEntity())}")
    }
}