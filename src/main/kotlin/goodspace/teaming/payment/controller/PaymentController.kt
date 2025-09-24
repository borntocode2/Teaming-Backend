package goodspace.teaming.payment.controller

import goodspace.teaming.email.dto.EmailVerifyRequestDto
import goodspace.teaming.payment.dto.PaymentApproveRespondDto
import goodspace.teaming.payment.dto.PaymentVerifyRequestDto
import goodspace.teaming.payment.dto.PaymentVerifyRespondDto
import goodspace.teaming.payment.dto.toEntity
import goodspace.teaming.payment.service.PaymentService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/payment")
class PaymentController(
    private val paymentService: PaymentService
) {

    @GetMapping(value = ["/html"], produces = [MediaType.TEXT_HTML_VALUE])
    fun showPaymentPage(
        @RequestBody paymentVerifyRequestDto: PaymentVerifyRequestDto,
    ): String {
        return """<!DOCTYPE html>
                <html lang="en">
                <head>
                <meta charset="UTF-8">
                <title>NicePay 결제</title>
                <script src="https://pay.nicepay.co.kr/v1/js/"></script>
                </head>
                <body onload="serverAuth()">

                <script>
                    function serverAuth() {
                    AUTHNICE.requestPay({
                    clientId: 'S2_fb903ce81792411ab6c459ec3a2a82c6',
                    method: 'card',
                    appScheme: `nicepaysample://`,
                    orderId: '${paymentService.generateUUIDString()}',
                    amount: ${paymentVerifyRequestDto.amount},
                    goodsName: '${paymentVerifyRequestDto.goodsName},}',
                    returnUrl: 'http://13.125.193.243:8080/payment/request',
                        fnError: function (result) {
                        alert('개발자확인용 : ' + result.errorMsg);
                        }
                    });
                }   
                </script>

                </body>
                </html>"""
    }

    @PostMapping("/request")
    fun requestPayment(@RequestParam params: MultiValueMap<String, String>): ResponseEntity<HttpStatus> {
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
        paymentService.savePaymentResult(paymentApproveRespondDto.toEntity())

        return paymentService.mapResultCodeToHttpStatus(paymentApproveRespondDto.resultCode)
    }
}