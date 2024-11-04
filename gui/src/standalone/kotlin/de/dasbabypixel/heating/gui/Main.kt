package de.dasbabypixel.heating.gui

import org.slf4j.bridge.SLF4JBridgeHandler
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration

fun main() {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()

    SpringApplication.run(GuiApplication::class.java)
}

@SpringBootApplication(exclude = [ErrorMvcAutoConfiguration::class])
class GuiApplication
