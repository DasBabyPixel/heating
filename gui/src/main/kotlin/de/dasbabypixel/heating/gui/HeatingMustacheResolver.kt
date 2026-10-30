package de.dasbabypixel.heating.gui

import com.github.mustachejava.MustacheResolver
import java.io.Reader

object HeatingMustacheResolver : MustacheResolver {
    override fun getReader(resourceName: String): Reader? {
        return WebAssets.resolve(resourceName)?.openStream()?.bufferedReader()
    }
}
