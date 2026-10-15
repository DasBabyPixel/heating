package de.dasbabypixel.heating.gui

import com.sun.net.httpserver.HttpServer
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.Executors

class GuiWebServer {
    private val sessionManager: SessionManager = SessionManager()
    private val server: HttpServer
    private var started = false

    constructor(
        host: InetAddress?,
        port: Int
    ) {
        server = HttpServer.create(InetSocketAddress(host, port), 0)
        server.executor = Executors.newVirtualThreadPerTaskExecutor()

        server.createContext("/") { exchange ->
            val path = exchange.requestURI.path

            val uuid = TokenAuthFilter.get(exchange)


            exchange.responseHeaders.set("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(200, 6)
            exchange.responseBody.use { it.write("Hello!".toByteArray()) }
        }.also {
            it.filters.add(CookieExtractingFilter())
            it.filters.add(TokenAuthFilter(sessionManager, "Session"))
        }
    }

    fun start() {
        if (started) throw IllegalStateException()
        started = true

        server.start()
    }

    fun stop() {
        server.stop(5)
    }
}
