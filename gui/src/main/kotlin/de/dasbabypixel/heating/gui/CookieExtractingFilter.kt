package de.dasbabypixel.heating.gui

import com.sun.net.httpserver.Filter
import com.sun.net.httpserver.HttpExchange

class CookieExtractingFilter : Filter() {
    override fun doFilter(
        exchange: HttpExchange,
        chain: Chain
    ) {
        val cookies = HashMap<String, String>()
        exchange.requestHeaders["Cookie"]?.stream()?.flatMap { value ->
            value.split(";").stream()
        }?.toList()?.forEach { cookie ->
            val parts = cookie.split("=", limit = 2)
            println(parts)
            if (parts.size == 2) {
                cookies[parts[0]] = parts[1]
            } else {
                exchange.sendResponseHeaders(400, -1)
                exchange.close()
                return
            }
        }
        exchange.setAttribute(ATTRIBUTE, cookies)
        chain.doFilter(exchange)
    }

    override fun description(): String {
        return "Extracts cookies into a \"cookies\" attribute typed as Map<String, String> for faster access"
    }

    companion object {
        private const val ATTRIBUTE = "cookies"

        @Suppress("UNCHECKED_CAST")
        fun get(
            exchange: HttpExchange,
            cookie: String
        ): String? {
            return (exchange.getAttribute(ATTRIBUTE) as Map<String, String>)[cookie]
        }
    }
}