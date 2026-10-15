package de.dasbabypixel.heating.gui

import java.net.URLConnection

object MimeTypeUtil {
    fun forPath(path: String): String {
        return when {
            path.endsWith(".html") -> "text/html; charset=utf-8"
            path.endsWith(".css") -> "text/css; charset=utf-8"
            path.endsWith(".js") || path.endsWith(".mjs") -> "application/javascript; charset=utf-8"
            path.endsWith(".json") -> "application/json; charset=utf-8"
            path.endsWith(".svg") -> "image/svg+xml"
            path.endsWith(".png") -> "image/png"
            path.endsWith(".jpg") || path.endsWith(".jpeg") -> "image/jpeg"
            path.endsWith(".ico") -> "image/x-icon"
            else -> URLConnection.guessContentTypeFromName(path) ?: "application/octet-stream"
        }
    }
}
