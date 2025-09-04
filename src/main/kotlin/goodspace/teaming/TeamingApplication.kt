package goodspace.teaming

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class TeamingApplication

fun main(args: Array<String>) {
    runApplication<TeamingApplication>(*args)
}
