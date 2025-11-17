package goodspace.teaming.push.service

import goodspace.teaming.global.repository.ExpoPushTokenRepository
import goodspace.teaming.push.dto.ExpoPushRequestDto
import goodspace.teaming.push.dto.ExpoPushResponseDto
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBody

@Service
class PushNotificationService(
    @Qualifier("expoWebClient")
    private val webClient: WebClient,

    private val tokenService: PushTokenService,
    private val tokenRepository: ExpoPushTokenRepository
) {
    private val logger = KotlinLogging.logger { }

    @Transactional(readOnly = true)
    fun send(
        userIds: List<Long>,
        title: String,
        body: String,
        data: Map<String, Any>? = null
    ) {
        val tokens = tokenRepository.findAllByUserIds(userIds)

        tokens.forEach { token ->
            CoroutineScope(Dispatchers.IO).launch {
                sendSinglePush(token.value, title, body, data, token.id!!)
            }
        }
    }

    private suspend fun sendSinglePush(
        tokenValue: String,
        title: String,
        body: String,
        data: Map<String, Any>?,
        tokenId: Long
    ) {
        val request = ExpoPushRequestDto(
            to = tokenValue,
            title = title,
            body = body,
            data = data
        )

        try {
            val response = webClient.post()
                .uri(PUSH_SEND_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .awaitBody<ExpoPushResponseDto>()

            if (response.shouldRemoveToken) {
                deleteToken(tokenId)
            } else if (response.isError) {
                logger.warn { "Expo 푸시 전송 실패: ${response.data.message}" }
            }

        } catch (e: WebClientResponseException) {
            logger.error(e) { "Expo API 호출 실패: ${e.responseBodyAsString}" }
        } catch (e: Exception) {
            logger.error(e) { "푸시 전송 중 알 수 없는 오류" }
        }
    }

    private suspend fun deleteToken(tokenId: Long) {
        withContext(Dispatchers.IO) {
            tokenService.delete(tokenId)
        }
    }

    private val ExpoPushResponseDto.shouldRemoveToken: Boolean
        get() {
            return isError && isDeviceNotRegistered
        }

    private val ExpoPushResponseDto.isError: Boolean
        get() {
            return data.status == ERROR
        }

    private val ExpoPushResponseDto.isDeviceNotRegistered: Boolean
        get() {
            return data.details?.error == DEVICE_NOT_REGISTERED
        }

    companion object {
        private const val PUSH_SEND_URL = "/push/send"

        private const val ERROR = "error"
        private const val DEVICE_NOT_REGISTERED = "DeviceNotRegistered"
    }
}
