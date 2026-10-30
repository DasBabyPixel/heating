package de.dasbabypixel.heating.gui.servlets

import com.github.mustachejava.DefaultMustacheFactory
import de.dasbabypixel.heating.gui.HeatingMustacheResolver
import de.dasbabypixel.heating.gui.MimeTypeUtil
import de.dasbabypixel.heating.gui.WebAssets
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.eclipse.jetty.ee10.servlet.ServletContextHandler
import org.eclipse.jetty.ee10.servlet.ServletHolder

abstract class AssetServletBase(val assetPath: String) : HttpServlet() {
    val asset = WebAssets.resolve(assetPath) ?: throw NullPointerException("Missing asset $assetPath")
    val contentType = MimeTypeUtil.forPath(assetPath)
}

open class MustacheServlet(assetPath: String) : AssetServletBase(assetPath) {
    val mustache =
        DefaultMustacheFactory(HeatingMustacheResolver).compile(asset.openStream().bufferedReader(), asset.path)!!
}

class AssetServlet(assetPath: String) : AssetServletBase(assetPath) {
    override fun doGet(
        req: HttpServletRequest,
        resp: HttpServletResponse
    ) {
        resp.contentType = contentType
        resp.status = HttpServletResponse.SC_OK
        asset.openStream().use {
            it.transferTo(resp.outputStream)
        }
    }
}

fun ServletContextHandler.addAssetServlet(
    assetPath: String,
    pathSpec: String = "/$assetPath"
) {
    addServlet(ServletHolder(AssetServlet(assetPath)), pathSpec)
}
