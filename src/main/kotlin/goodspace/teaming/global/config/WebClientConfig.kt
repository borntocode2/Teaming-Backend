package goodspace.teaming.global.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

    @Bean
    @Qualifier("expoWebClient")
    fun expoWebClient(webClientBuilder: WebClient.Builder): WebClient {
        return webClientBuilder
            .baseUrl("https://exp.host/--/api/v2")
            .defaultHeader("Content-Type", "application/json")
            .build()
    }
}
