package goodspace.teaming.global.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val tokenProvider: TokenProvider
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .httpBasic { it.disable() }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .formLogin { it.disable() }
            .logout { it.disable() }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/swagger-ui/**").permitAll() // swagger
                    .requestMatchers("/v3/api-docs/**").permitAll() // SpringDoc
                    .requestMatchers("/email/**").permitAll()
                    .requestMatchers("/ws/**").permitAll() // 웹소캣 핸드셰이크 1
                    .requestMatchers(("/ws-sockjs/**")).permitAll() // 웹소캣 핸드셰이크 2
                    .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll() // 프리플라이트 허용
                    .requestMatchers("/landing/**").permitAll() // 랜딩 페이지
                    .requestMatchers("/api/auth/**").permitAll() // 회원가입
                    .requestMatchers(("/users/me/access-token")).permitAll() // 엑세스 토큰 재발급
                    .requestMatchers("/static/**", "/nicepay-test.html").permitAll()
                    .requestMatchers("/payment/**").permitAll()
                    .requestMatchers("/admin/**").hasRole("ADMIN") // 관리자
                    .anyRequest().authenticated()
            }
            .cors { it.configurationSource(configurationSource()) }
            .addFilterBefore(JwtFilter(tokenProvider), UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }

    @Bean
    fun configurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            // TODO: 웹 배포 후 웹의 origin으로 구체화
            allowedOriginPatterns = listOf("*")
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            exposedHeaders = listOf("Access-Control-Allow-Credentials", "Authorization", "Set-Cookie")
            allowCredentials = true
            maxAge = 3600
        }
        return UrlBasedCorsConfigurationSource().also {
            it.registerCorsConfiguration("/**", config)
        }
    }
}
