package goodspace.teaming

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.retry.annotation.EnableRetry

@SpringBootApplication
@EnableJpaAuditing
@EnableRetry
class TeamingApplication

fun main(args: Array<String>) {
    runApplication<TeamingApplication>(*args)
}
