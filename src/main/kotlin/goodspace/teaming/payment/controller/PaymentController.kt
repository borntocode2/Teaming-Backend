package goodspace.teaming.payment.controller

import goodspace.teaming.global.security.getUserId
import goodspace.teaming.payment.dto.PaymentRoomIdDto
import goodspace.teaming.payment.dto.PaymentVerifyRespondDto
import goodspace.teaming.payment.service.PaymentService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/payment")
class PaymentController(
    private val paymentService: PaymentService
) {

    @GetMapping(value = ["/html"], produces = [MediaType.TEXT_HTML_VALUE])
    fun showPaymentPage(@RequestParam amount: Long, principal: Principal, @RequestParam roomId: Long, @RequestParam platform: String
    ): String {
        val userId = principal.getUserId()
        val userPayInfo = "$userId:$roomId:$platform"

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
                    appScheme: `teaming://`,
                    orderId: '${paymentService.generateUUIDString()}',
                    amount: ${amount},
                    mallReserved: '${userPayInfo}',
                    goodsName: 'Room Create',
                    returnUrl: 'https://teamingkr.duckdns.org/api/payment/request',
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
    fun requestPayment(@RequestParam params: MultiValueMap<String, String>): ResponseEntity<Void> {
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

        return paymentService.requestApprove(dto)
    }

    @PostMapping("/cancelAuth")
    fun requestCancel(@RequestBody paymentRoomIdDto: PaymentRoomIdDto): ResponseEntity<Void> {
        return paymentService.requestCancel(paymentRoomIdDto.roomId.toLong())
    }
}