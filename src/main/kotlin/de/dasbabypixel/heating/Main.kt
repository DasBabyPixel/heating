package de.dasbabypixel.heating

import org.slf4j.bridge.SLF4JBridgeHandler
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

fun main() {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()
    Runtime.getRuntime().addShutdownHook(Thread {
        println("Shutdown initiated")
    })

    SpringApplication.run(DemoApplication::class.java)
}

@SpringBootApplication
class DemoApplication
