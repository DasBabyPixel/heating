package de.dasbabypixel.heating.gui

import de.dasbabypixel.heating.gui.LoadedResourcesIndex.LoadedFile
import java.io.InputStream

object WebAssets : Iterable<WebAsset> {
    private val assets = LoadedResourcesIndex.load("/assets/web")

    fun resolve(path: String): WebAsset? {
        return assets.resolveFile(path)?.let { WebAsset(path, it) }
    }

    override fun iterator(): Iterator<WebAsset> {
        return assets.iterator()
            .asSequence()
            .filter { it.loadedElement is LoadedFile }
            .map { WebAsset(it.path, it.loadedElement as LoadedFile) }
            .iterator()
    }
}

class WebAsset(
    val path: String,
    private val file: LoadedFile
) {
    val size: ULong
        get() = file.size

    fun openStream(): InputStream {
        return file.openStream()
    }
}
