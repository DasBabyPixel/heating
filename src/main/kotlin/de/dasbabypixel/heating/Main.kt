package de.dasbabypixel.heating

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration

fun main() {
    SpringApplication.run(DemoApplication::class.java)
}

@SpringBootApplication(exclude = [ErrorMvcAutoConfiguration::class])
class DemoApplication
