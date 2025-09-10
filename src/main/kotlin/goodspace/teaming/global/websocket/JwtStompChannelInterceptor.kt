package goodspace.teaming.global.websocket

import goodspace.teaming.chat.service.RoomAccessAuthorizer
import goodspace.teaming.global.security.TokenProvider
import goodspace.teaming.global.security.TokenType
import goodspace.teaming.global.security.getUserId
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component

private const val ROOM_SUBSCRIBE_PREFIX = "/topic/rooms/"
private const val ROOM_SEND_PREFIX = "/app/rooms/"

@Component
class JwtStompChannelInterceptor(
    private val tokenProvider: TokenProvider,
    private val roomAccessAuthorizer: RoomAccessAuthorizer
) : ChannelInterceptor {
    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val accessor = StompHeaderAccessor.wrap(message)

        if (accessor.command == StompCommand.CONNECT) {
            val token = getTokenFromNativeHeader(accessor, "Authorization")
            validateToken(token)

            setAuthenticationOnAccessor(accessor, token)
        }

        if (accessor.command == StompCommand.SUBSCRIBE) {
            val destination = accessor.destination.orEmpty()

            if (destination.startsWith(ROOM_SUBSCRIBE_PREFIX)) {
                val roomId = getRoomIdFromDestination(destination)

                val userId = accessor.user?.getUserId()
                    ?: throw IllegalStateException("인증 정보 없음")

                roomAccessAuthorizer.assertMemberOf(roomId, userId)
            }
        }

        if (accessor.command == StompCommand.SEND) {
            val destination = accessor.destination.orEmpty()
            if (destination.startsWith(ROOM_SEND_PREFIX) && destination.endsWith("/send")) {
                val roomId = destination.removePrefix(ROOM_SEND_PREFIX).substringBefore('/').toLongOrNull()
                    ?: throw IllegalArgumentException("잘못된 발행 경로: $destination")

                val userId = accessor.user?.getUserId()
                    ?: throw IllegalStateException("인증 정보 없음")

                roomAccessAuthorizer.assertMemberOf(roomId, userId)
            }
        }

        return MessageBuilder.createMessage(message.payload, accessor.messageHeaders)
    }

    private fun getTokenFromNativeHeader(
        accessor: StompHeaderAccessor,
        headerName: String
    ): String {
        val rawToken = accessor.getFirstNativeHeader(headerName).orEmpty()

        return rawToken
            .removePrefix("Bearer ")
            .ifEmpty { rawToken.removePrefix("bearer ") }
            .trim()
    }

    private fun validateToken(token: String) {
        require(token.isNotBlank() && tokenProvider.validateToken(token, TokenType.ACCESS)) { "부적절한 토큰입니다." }
    }

    private fun setAuthenticationOnAccessor(
        accessor: StompHeaderAccessor,
        token: String
    ) {
        accessor.user = tokenProvider.getAuthentication(token)
    }

    private fun getRoomIdFromDestination(destination: String): Long {
        return destination.removePrefix(ROOM_SUBSCRIBE_PREFIX).substringBefore('/').toLongOrNull()
            ?: throw IllegalArgumentException("잘못된 구독 경로: $destination")
    }
}
