package de.dasbabypixel.heating.gui

import java.io.InputStream
import java.net.URL

object JarAssets {
    private const val ASSETS_ROOT = "assets"
    fun loadWeb(path: String): Asset? {
        return load("web/$path")
    }

    fun load(path: String): Asset? {
        if (path.startsWith("/")) return null
        val resource = javaClass.classLoader.getResource("$ASSETS_ROOT/$path") ?: return null
        return Asset(path, resource)
    }
}

data class Asset(
    val path: String,
    val url: URL
) {
    fun openStream(): InputStream {
        return url.openStream()
    }
}
