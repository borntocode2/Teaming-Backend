package goodspace.teaming.payment.service

import goodspace.teaming.payment.config.NicepayProperties

class PaymentService(
    private val nicepayProperties: NicepayProperties,
    private val webClient: WebClient.Builder
){

}