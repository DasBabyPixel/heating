package de.dasbabypixel.heating.gui

import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler
import java.net.InetAddress
import kotlin.io.path.Path
import kotlin.system.measureTimeMillis

fun main() {
    val millis = measureTimeMillis {
        SLF4JBridgeHandler.install()
        Gui.logger.info("Booting...")

        val applicationSecretKey = ApplicationSecretKey.getOrCreateFromFile(Path(".secret.key"))

        val host: InetAddress? = null
        val port = 8041
        val server = GuiWebServer(applicationSecretKey, host, port)

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                Gui.logger.info("Shutting down...")
                server.stop()
                Gui.logger.info("Goodbye!")
            }
        })
        server.start()
    }
    Gui.logger.info("Started successfully in {}ms", millis)
}

class Gui {
    companion object {
        val logger = LoggerFactory.getLogger("GUI")
    }
}