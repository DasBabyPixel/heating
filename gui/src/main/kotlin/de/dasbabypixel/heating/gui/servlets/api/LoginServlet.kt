package de.dasbabypixel.heating.gui.servlets.api

import com.fasterxml.jackson.databind.ObjectMapper
import de.dasbabypixel.heating.gui.SessionManager
import de.dasbabypixel.heating.gui.UserPrincipal
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.eclipse.jetty.http.MimeTypes.Type
import kotlin.io.encoding.Base64
import kotlin.uuid.Uuid

class LoginServlet(val sessionManager: SessionManager) : HttpServlet() {
    private val mapper = ObjectMapper()
    override fun doPost(
        req: HttpServletRequest,
        resp: HttpServletResponse
    ) {
        val user = req.getParameter("username")
        val pass = req.getParameter("password")

        if ("admin" == user && "secpass" == pass) { // TODO
            val principal = UserPrincipal("admin", Uuid.random())
            resp.addCookie(Cookie("identity", sessionManager.sign(principal)).also {
                it.setAttribute("SameSite", "Strict")
                it.path = "/"
                it.isHttpOnly = true
            })

            val redirectCookie =
                req.cookies?.find { it.name == "redirect" }?.value?.let { Base64.decode(it) }?.decodeToString()

            val jsonResponse = mapper.createObjectNode()
            if (redirectCookie != null) {
                jsonResponse.put("redirect", redirectCookie)
                resp.addCookie(Cookie("redirect", "").apply {
                    path = "/api/login"
                    maxAge = 0
                })
            }

            resp.status = HttpServletResponse.SC_OK
            resp.contentType = Type.APPLICATION_JSON.asString()
            resp.writer.write(mapper.writeValueAsString(jsonResponse))
        } else {
            resp.contentType = Type.TEXT_HTML.asString()
            resp.status = HttpServletResponse.SC_UNAUTHORIZED
            resp.writer.write("Invalid credentials")
        }
    }
}