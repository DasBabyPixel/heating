package de.dasbabypixel.heating

import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

fun main() {
    LoggerFactory.getLogger("TestThis").info("Information")
    SpringApplication.run(DemoApplication::class.java)
}

@SpringBootApplication
@RestController
class DemoApplication {
    @GetMapping("/test")
    fun test(
        @RequestParam(value = "name", defaultValue = "World")
        name: String
    ): String {
        return "Hello $name"
    }
}