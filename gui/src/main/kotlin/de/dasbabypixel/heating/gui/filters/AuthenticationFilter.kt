package de.dasbabypixel.heating.gui.filters

import de.dasbabypixel.heating.gui.SessionManager
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class AuthenticationFilter(val sessionManager: SessionManager) : Filter {
    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain
    ) {
        request as HttpServletRequest
        response as HttpServletResponse

        val identityCookie = request.cookies?.find { it.name == "identity" }?.value
        if (identityCookie != null) {
            val principal = sessionManager.verifyAndGet(identityCookie)
            if (principal != null) {
                request.setAttribute("principal", principal)
            }
        }
        chain.doFilter(request, response)
    }
}
