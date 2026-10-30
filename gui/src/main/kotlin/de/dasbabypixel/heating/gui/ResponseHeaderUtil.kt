package de.dasbabypixel.heating.gui

// fun HttpExchange.setCookie(
//     name: String,
//     value: String,
//     expires: TemporalAccessor? = null,
//     httpOnly: Boolean = false,
//     maxAge: Long? = null,
//     path: String? = null,
//     secure: Boolean = false
// ) {
//     val b = StringBuilder(name).append("=").append(value)
//     if (expires != null) b.append("; Expires=").append(DateUtil.format(expires))
//     if (httpOnly) b.append("; HttpOnly")
//     if (maxAge != null) b.append("; Max-Age=").append(maxAge)
//     if (path != null) b.append("; Path=").append(path)
//     if (secure) b.append("; Secure")
//     responseHeaders.add("Set-Cookie", b.toString())
// }
//
// enum class SameSite {
//     STRICT("Strict"),
//     LAX("Lax"),
//     NONE("None");
//
//     private val s: String
//
//     constructor(s: String) {
//         this.s = s
//     }
//
//     override fun toString(): String {
//         return s
//     }
// }
