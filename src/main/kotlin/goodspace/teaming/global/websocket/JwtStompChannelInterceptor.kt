package goodspace.teaming.global.websocket

import goodspace.teaming.chat.service.RoomAccessAuthorizer
import goodspace.teaming.global.security.TokenProvider
import goodspace.teaming.global.security.TokenType
import goodspace.teaming.global.security.getUserId
import org.slf4j.LoggerFactory
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import java.security.Principal
import kotlin.math.min

private const val ROOM_SUBSCRIBE_PREFIX = "/topic/rooms/"
private const val ROOM_SEND_PREFIX = "/app/rooms/"
private const val AUTH_HEADER = "Authorization"
private const val AUTH_SESSION_KEY = "WS_AUTHENTICATION"
private const val MAX_LOG_PAYLOAD_LENGTH = 512

@Component
class JwtStompChannelInterceptor(
    private val tokenProvider: TokenProvider,
    private val roomAccessAuthorizer: RoomAccessAuthorizer
) : ChannelInterceptor {

    private val log = LoggerFactory.getLogger(JwtStompChannelInterceptor::class.java)

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val accessor = StompHeaderAccessor.wrap(message)

        when (accessor.command) {
            StompCommand.CONNECT -> handleConnect(accessor)
            StompCommand.SUBSCRIBE -> handleSubscribe(accessor)
            StompCommand.SEND -> handleSend(accessor, message)
            else -> { /* no-op */ }
        }

        return MessageBuilder.createMessage(message.payload, accessor.messageHeaders)
    }

    private fun handleConnect(accessor: StompHeaderAccessor) {
        val token = getTokenFromNativeHeader(accessor, AUTH_HEADER)
        validateToken(token)

        val authentication = tokenProvider.getAuthentication(token)
        accessor.user = authentication
        accessor.sessionAttributes?.put(AUTH_SESSION_KEY, authentication)

        log.info("웹소켓 CONNECT userId=${authentication.getUserId()}")
    }

    private fun handleSubscribe(accessor: StompHeaderAccessor) {
        ensurePrincipal(accessor)

        val destination = accessor.destination.orEmpty()
        if (destination.startsWith(ROOM_SUBSCRIBE_PREFIX)) {
            val roomId = getRoomIdFromDestination(destination)
            val userId = accessor.user?.getUserId()
                ?: throw IllegalStateException("인증 정보 없음")

            roomAccessAuthorizer.assertMemberOf(roomId, userId)
            log.info("웹소켓 SUBSCRIBE userId=$userId roomId=$roomId")
        }
    }

    private fun handleSend(accessor: StompHeaderAccessor, message: Message<*>) {
        ensurePrincipal(accessor)

        val destination = accessor.destination.orEmpty()
        if (destination.startsWith(ROOM_SEND_PREFIX) && destination.endsWith("/send")) {
            val roomId = destination.removePrefix(ROOM_SEND_PREFIX).substringBefore('/').toLongOrNull()
                ?: throw IllegalArgumentException("잘못된 발행 경로: $destination")

            val userId = accessor.user?.getUserId()
                ?: throw IllegalStateException("인증 정보 없음")

            roomAccessAuthorizer.assertMemberOf(roomId, userId)
            log.info(
                "웹소켓 SEND userId=$userId roomId=$roomId payload=${formatPayloadForLog(message.payload)}"
            )
        }
    }

    /**
     * CONNECT 이후 프레임에서 accessor.user 가 누락될 수 있으니
     * 1) 세션에서 복원하고, 2) (옵션) 프레임에 Authorization 헤더가 있으면 재인증 지원
     */
    private fun ensurePrincipal(accessor: StompHeaderAccessor) {
        if (accessor.user != null) return

        val sessionAuth = accessor.sessionAttributes?.get(AUTH_SESSION_KEY)
        if (sessionAuth is Principal) {
            accessor.user = sessionAuth
            return
        }

        val token = getTokenFromNativeHeader(accessor, AUTH_HEADER)
        if (token.isNotBlank() && tokenProvider.validateToken(token, TokenType.ACCESS)) {
            accessor.user = tokenProvider.getAuthentication(token)
            accessor.sessionAttributes?.put(AUTH_SESSION_KEY, accessor.user!!)
        }
    }

    private fun getTokenFromNativeHeader(
        accessor: StompHeaderAccessor,
        headerName: String
    ): String {
        val raw = accessor.getFirstNativeHeader(headerName).orEmpty().trim()

        return if (raw.startsWith("Bearer ", ignoreCase = true)) {
            raw.substring(7).trim() // "Bearer " 제거
        } else {
            raw
        }
    }

    private fun validateToken(token: String) {
        require(token.isNotBlank() && tokenProvider.validateToken(token, TokenType.ACCESS)) {
            "부적절한 토큰입니다."
        }
    }

    private fun getRoomIdFromDestination(destination: String): Long {
        return destination.removePrefix(ROOM_SUBSCRIBE_PREFIX)
            .substringBefore('/')
            .toLongOrNull()
            ?: throw IllegalArgumentException("잘못된 구독 경로: $destination")
    }

    private fun formatPayloadForLog(payload: Any?): String {
        if (payload == null) return "null"
        val text = when (payload) {
            is ByteArray -> runCatching { String(payload) }.getOrElse { "<binary:${payload.size} bytes>" }
            else -> payload.toString()
        }
        val end = min(text.length, MAX_LOG_PAYLOAD_LENGTH)
        return if (end < text.length) text.substring(0, end) + "...(truncated)" else text
    }
}
