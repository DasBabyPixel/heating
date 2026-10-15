package de.dasbabypixel.heating.webapi

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.Executors

class WebServer {
    private val port: Int

    fun start() {
    }

    class Builder {
        var hostname: String = "::/0"
        var port: Int = 80
        private val handlers = HashMap<String, HttpHandler>()

        fun build(): WebServer {
            val address = InetSocketAddress(hostname, port)
            val server = HttpServer.create(address, 0)
            server.executor = Executors.newVirtualThreadPerTaskExecutor()

            handlers.entries.forEach {
                val ctx = server.createContext("")

            }
        }
    }
}