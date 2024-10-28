package de.dasbabypixel.heating.gui

import org.slf4j.bridge.SLF4JBridgeHandler
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

fun main() {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()

    SpringApplication.run(GuiApplication::class.java)
}

@SpringBootApplication
class GuiApplication
