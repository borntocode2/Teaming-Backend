package goodspace.teaming.payment.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "nicepay")
@Configuration
class NicepayProperties {
    lateinit var clientId: String
    lateinit var secretKey: String
    lateinit var approveUrl: String
}