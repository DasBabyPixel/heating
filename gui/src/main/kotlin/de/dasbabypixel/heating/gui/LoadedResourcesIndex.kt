package de.dasbabypixel.heating.gui

import de.dasbabypixel.heating.gui.LoadedResourcesIndex.LoadedElementWithPath
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader

class LoadedResourcesIndex(private val root: LoadedDirectory) : Iterable<LoadedElementWithPath> {
    fun resolveFile(path: String): LoadedFile? {
        val file = root.resolve(path)
        if (file !is LoadedFile) return null
        return file
    }

    override fun iterator(): Iterator<LoadedElementWithPath> {
        return object : Iterator<LoadedElementWithPath> {
            private val iteratorStack = ArrayDeque<Pair<String, Iterator<Map.Entry<String, LoadedElement>>>>().apply {
                addFirst("" to root.map.entries.iterator())
            }
            private var nextPrepared: LoadedElementWithPath? = null

            private fun advance(): Boolean {
                while (!iteratorStack.isEmpty()) {
                    val (currentPath, iter) = iteratorStack.first()
                    if (iter.hasNext()) {
                        val entry = iter.next()
                        val name = entry.key
                        val element = entry.value
                        val path = if (currentPath.isEmpty()) name else "$currentPath/$name"

                        nextPrepared = LoadedElementWithPath(element, path)

                        if (element is LoadedDirectory) {
                            iteratorStack.addFirst(path to element.map.entries.iterator())
                        }
                        return true
                    } else {
                        iteratorStack.removeFirst()
                    }
                }
                return false
            }

            override fun hasNext(): Boolean {
                return nextPrepared != null || advance()
            }

            override fun next(): LoadedElementWithPath {
                if (!hasNext()) throw NoSuchElementException()
                val result = nextPrepared!!
                nextPrepared = null
                return result
            }
        }
    }

    companion object {
        fun load(rootPath: String): LoadedResourcesIndex {
            return LoadedResourcesIndex(
                tryLoadDir("", rootPath) ?: throw NullPointerException("Unable to find root at $rootPath")
            )
        }

        private fun loadElement(
            name: String,
            path: String
        ): LoadedElement {
            return tryLoadDir(name, path) ?: loadFile(name, path)
        }

        private fun loadFile(
            name: String,
            path: String
        ): LoadedFile {
            val bytes = LoadedResourcesIndex::class.java.getResource(path)!!.readBytes()
            return LoadedFile(name, bytes.size.toULong()) {
                ByteArrayInputStream(bytes)
            }
        }

        private fun tryLoadDir(
            name: String,
            path: String
        ): LoadedDirectory? {
            val rootIndexURL = LoadedResourcesIndex::class.java.getResource("$path/.index.dat") ?: return null
            val list = InputStreamReader(rootIndexURL.openStream()).buffered().use { reader ->
                ArrayList<String>().apply {
                    while (true) {
                        val str = reader.readLine()
                        if (str == null || str.isEmpty()) break
                        add(str)
                    }
                }
            }
            val map = list.associateWith { name -> loadElement(name, "$path/$name") }
            return LoadedDirectory(name, map)
        }
    }

    class LoadedElementWithPath(
        val loadedElement: LoadedElement,
        val path: String
    )

    sealed interface LoadedElement

    class LoadedDirectory(
        val name: String,
        val map: Map<String, LoadedElement>
    ) : LoadedElement {
        fun resolve(path: String): LoadedElement? {
            val idx = path.indexOf('/')
            if (idx == -1) {
                return map[path]
            }
            val dir = map[path.substring(0, idx)]
            if (dir !is LoadedDirectory) return null
            return dir.resolve(path.substring(idx + 1))
        }
    }

    class LoadedFile(
        val name: String,
        val size: ULong,
        private val inputStreamSupplier: () -> InputStream
    ) : LoadedElement {
        fun openStream(): InputStream {
            return inputStreamSupplier()
        }
    }
}
