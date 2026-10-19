package de.dasbabypixel.heating.gui

import org.slf4j.LoggerFactory
import org.slf4j.bridge.SLF4JBridgeHandler
import java.net.InetAddress
import kotlin.system.measureTimeMillis

fun main() {
    val millis = measureTimeMillis {
        SLF4JBridgeHandler.install()
        Gui.logger.info("Booting...")
        val host: InetAddress? = null
        val port = 8041
        val server = GuiWebServer(host, port)
        Gui::class.java.let {
            it.getResource("/assets/web")!!.let { url ->
                url.openConnection().let { con ->
                    println(con)
                    println(con.contentType)
                }
            }

            it.module.layer.configuration().findModule(it.module.name).orElseThrow().let { res ->
                res.reference().let { ref ->
                    ref.open().use { reader ->
                        reader.list().forEach {
                            println(it)
                        }
                    }
                }
            }


            println(it.getResource("/assets/web"))
            println(it.getResource("/assets/web/"))
            println(it.getResource("/assets/web/favicon.ico"))
            println(it.getResource("assets/web/favicon.ico"))
            println(it.getResource("assets/web"))
            println(it.getResource("assets/web/"))
            println(it.classLoader.getResource("/assets/web"))
            println(it.classLoader.getResource("/assets/web/"))
            println(it.classLoader.getResource("/assets/web/favicon.ico"))
        }
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