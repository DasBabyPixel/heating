package de.dasbabypixel.heating.gui.filters

import de.dasbabypixel.heating.gui.UserPrincipal
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.eclipse.jetty.http.HttpHeader
import kotlin.io.encoding.Base64

class RequiresLoggedInFilter(val excludes: Set<String>) : Filter {
    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain
    ) {
        request as HttpServletRequest
        response as HttpServletResponse
        if (!excludes.contains(request.servletPath)) {
            val principal = request.getAttribute("principal") as UserPrincipal?
            if (principal == null) {
                val redirectPath = request.servletPath.let { it.ifEmpty { "/" } }
                response.addCookie(Cookie("redirect", Base64.encode(redirectPath.toByteArray())).apply {
                    path = "/api/login"
                })
                response.setHeader(HttpHeader.LOCATION.name, "/login")
                response.status = HttpServletResponse.SC_SEE_OTHER
                return
            }
        }

        chain.doFilter(request, response)
    }
}
