package goodspace.teaming.global.websocket

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
class StompAuthConfig(
    private val jwtStompChannelInterceptor: JwtStompChannelInterceptor
) : WebSocketMessageBrokerConfigurer {
    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(jwtStompChannelInterceptor)
    }
}
