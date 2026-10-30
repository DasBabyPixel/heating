package de.dasbabypixel.heating.gui.servlets

import com.github.mustachejava.DefaultMustacheFactory
import de.dasbabypixel.heating.gui.HeatingMustacheResolver
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.io.StringWriter

class DashboardServlet : AssetServletBase("dashboard.html") {
    val mustache =
        DefaultMustacheFactory(HeatingMustacheResolver).compile(asset.openStream().bufferedReader(), asset.path)!!

    override fun doGet(
        req: HttpServletRequest,
        resp: HttpServletResponse
    ) {
        val scopes = HashMap<String, Any>()
        scopes["test"] = "Hi-Test!"
        val compiled = StringWriter().also {
            mustache.execute(it, scopes).flush()
        }.toString()
        resp.contentType = contentType
        resp.status = HttpServletResponse.SC_OK
        resp.writer.write(compiled)
    }
}
