package de.dasbabypixel.heating.gui

// class TokenAuthFilter(
//     val sessionManager: SessionManager,
//     val cookieName: String,
// ) : Filter() {
//     override fun doFilter(
//         exchange: HttpExchange,
//         chain: Chain
//     ) {
//         try {
//             val token = CookieExtractingFilter.get(exchange, cookieName)
//             val uuid = token?.let { sessionManager.verifyAndGet(it) }
//             // if (uuid == null) {
//             //     exchange.setCookie(LoginPage.REDIRECT_COOKIE, Base64.encode(path.encodeToByteArray()))
//             //     exchange.responseHeaders.add("Location", "/login")
//             //     exchange.sendResponseHeaders(303, -1)
//             //     exchange.close()
//             //
//             //     // exchange.sendResponseHeaders(401, -1)
//             //     // exchange.close()
//             //     return
//             // }
//             exchange.setAttribute(ATTRIBUTE, uuid)
//             chain.doFilter(exchange)
//         } catch (e: Exception) {
//             e.printStackTrace()
//         }
//     }
//
//     override fun description(): String = "denies unauthenticated requests"
//
//     companion object {
//         private const val ATTRIBUTE = "SESSION_ID"
//
//         fun get(exchange: HttpExchange): Uuid? {
//             return exchange.getAttribute(ATTRIBUTE) as Uuid?
//         }
//     }
// }
