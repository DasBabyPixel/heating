package de.dasbabypixel.heating.gui

import java.io.InputStream

class LoadedResourcesIndex(root: LoadedDirectory) {
    val root: LoadedDirectory = root

    companion object {
        fun load(rootPath: String): LoadedResourcesIndex {
            val rootIndexStream = LoadedResourcesIndex::class.java.getResourceAsStream("$rootPath/index.json")!!
            
        }
    }

    sealed interface LoadedElement

    class LoadedDirectory(val name: String, val map: Map<String, LoadedElement>) : LoadedElement

    class LoadedFile(val name: String, val inputStreamSupplier: () -> InputStream) : LoadedElement
}