package de.dasbabypixel.heating

import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration

fun main() {
    LoggerFactory.getLogger("TestThis").info("Information")
    val ctx = SpringApplication.run(DemoApplication::class.java)
}

@EnableAutoConfiguration(exclude = [ErrorMvcAutoConfiguration::class])
@SpringBootApplication
class DemoApplication
