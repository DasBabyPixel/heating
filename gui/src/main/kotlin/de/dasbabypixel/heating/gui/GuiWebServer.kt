package de.dasbabypixel.heating.gui

import de.dasbabypixel.heating.gui.filters.AuthenticationFilter
import de.dasbabypixel.heating.gui.filters.RequiresLoggedInFilter
import de.dasbabypixel.heating.gui.servlets.DashboardServlet
import de.dasbabypixel.heating.gui.servlets.StatusServlet
import de.dasbabypixel.heating.gui.servlets.addAssetServlet
import de.dasbabypixel.heating.gui.servlets.api.LoginServlet
import jakarta.servlet.DispatcherType
import org.eclipse.jetty.ee10.servlet.FilterHolder
import org.eclipse.jetty.ee10.servlet.ServletContextHandler
import org.eclipse.jetty.ee10.servlet.ServletHolder
import org.eclipse.jetty.server.HttpConfiguration
import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import java.net.InetAddress
import java.util.EnumSet

class GuiWebServer {
    private val sessionManager: SessionManager
    private val server: Server
    private var started = false

    constructor(
        applicationSecretKey: ApplicationSecretKey,
        host: InetAddress?,
        port: Int
    ) {
        sessionManager = SessionManager(applicationSecretKey)
        server = Server()

        val httpConfiguration = HttpConfiguration()
        httpConfiguration.sendServerVersion = false
        httpConfiguration.sendXPoweredBy = false

        val httpConnectionFactory = HttpConnectionFactory(httpConfiguration)
        val connector = ServerConnector(server, httpConnectionFactory)
        connector.host = host?.hostName
        connector.port = port

        server.addConnector(connector)

        val context = ServletContextHandler(ServletContextHandler.SESSIONS)

        context.addFilter(
            FilterHolder(AuthenticationFilter(sessionManager)), "/*", EnumSet.allOf(DispatcherType::class.java)
        )
        context.addFilter(
            FilterHolder(
                RequiresLoggedInFilter(
                    setOf(
                        "/api/login", "/login", "/login.css", "/login.js", "/test", "/favicon.ico", "/main.css"
                    )
                )
            ), "/*", EnumSet.allOf(DispatcherType::class.java)
        )

        context.addServlet(ServletHolder(LoginServlet(sessionManager)), "/api/login")
        context.addServlet(ServletHolder(StatusServlet()), "/status")

        context.addAssetServlet("favicon.ico")
        context.addAssetServlet("main.css")

        context.addServlet(ServletHolder(DashboardServlet()), "")

        context.addAssetServlet("login.html", "/login")
        context.addAssetServlet("login.css")
        context.addAssetServlet("login.js")


        server.handler = context
        server.errorHandler = ErrorHandler()

        // val endpointTree = EndpointTree.Builder().apply {
        //     add("stream", "GET", StreamEndpointHandler())
        // }.build()
        // server.createContext("/api/") { exchange ->
        //     try {
        //         val path = exchange.requestURI.path.let { path ->
        //             if (!path.startsWith("/api/")) {
        //                 exchange.sendTextPlainResponse(400, "Bad request for \"$path\"!")
        //                 return@createContext
        //             }
        //             path.substring(5)
        //         }
        //         val endpoint = endpointTree.resolve(path)
        //         if (endpoint == null) {
        //             exchange.sendTextPlainResponse(404, "Endpoint not found for \"$path\"!")
        //             return@createContext
        //         }
        //         val handler = endpoint.resolve(exchange.requestMethod)
        //         if (handler == null) {
        //             exchange.responseHeaders.set("Allow", endpoint.supportedMethods.joinToString())
        //             exchange.sendTextPlainResponse(405, "Method not found for \"$path\"!")
        //             return@createContext
        //         }
        //         handler.handle(EndpointEvent(endpoint, HttpExchange(exchange)))
        //     } catch (e: Exception) {
        //         e.printStackTrace()
        //     }
        // }.also {
        //     it.filters.add(CookieExtractingFilter())
        //     it.filters.add(TokenAuthFilter(sessionManager, "Session"))
        // }
        // server.createContext("/") { exchange ->
        //     try {
        //         val path = exchange.requestURI.path.let { path ->
        //             if (!path.startsWith("/")) {
        //                 exchange.sendTextPlainResponse(400, "Bad request for \"$path\"!")
        //                 return@createContext
        //             }
        //             path.substring(1)
        //         }
        //
        //         val uuid = TokenAuthFilter.get(exchange)
        //
        //         var asset = WebAssets.resolve(path)
        //         if (asset == null) {
        //             val fileNameIdx = path.lastIndexOf('/')
        //             val fileName = if (fileNameIdx == -1) path else path.substring(fileNameIdx + 1)
        //             if (!fileName.contains('.')) { // Try html
        //                 asset = WebAssets.resolve("$path.html")
        //             }
        //         }
        //         if (asset == null) {
        //             exchange.sendTextPlainResponse(404, "404 not found \"$path\"")
        //             return@createContext
        //         }
        //
        //         exchange.responseHeaders.set("Content-Type", MimeTypeUtil.forPath(asset.path))
        //         exchange.sendResponseHeaders(200, asset.size.toLong())
        //         exchange.responseBody.use { output ->
        //             asset.openStream().use { input ->
        //                 input.transferTo(output)
        //             }
        //         }
        //
        //     } catch (e: Exception) {
        //         e.printStackTrace()
        //     }
        // }.also {
        //     it.filters.add(CookieExtractingFilter())
        //     it.filters.add(TokenAuthFilter(sessionManager, "Session"))
        // }
    }

    // fun HttpExchange.sendTextPlainResponse(
    //     rCode: Int,
    //     response: String
    // ) {
    //     responseHeaders.set("Content-Type", "text/plain; charset=utf-8")
    //     sendByteResponse(rCode, response.encodeToByteArray())
    // }
    //
    // fun HttpExchange.sendByteResponse(
    //     rCode: Int,
    //     response: ByteArray
    // ) {
    //     sendResponseHeaders(rCode, response.size.toLong())
    //     responseBody.use { it.write(response) }
    // }

    fun start() {
        if (started) throw IllegalStateException()
        started = true

        server.start()
    }

    fun stop() {
        server.stop()
    }
}
