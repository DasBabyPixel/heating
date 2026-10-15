package de.dasbabypixel.heating.gui.endpoints

class EndpointTree {

    private val root = Root()

    fun resolve(path: String): Endpoint? {
        return root.resolve(path)
    }

    fun add(
        path: String,
        handler: EndpointHandler
    ) {
        val endpoint = Endpoint(path, handler)
        root.add(path, endpoint)
    }

    class Root : Node()

    class Child : Node()

    abstract class Node {
        private val children: MutableMap<String, Node> = HashMap()
        private val endpoints: MutableMap<String, Endpoint> = HashMap()

        fun resolve(path: String): Endpoint? {
            val slashIndex = path.indexOf('/')
            if (slashIndex == -1) { // No slash found, last node in tree
                return endpoints[path]
            } else {
                val child = children[path.substring(0, slashIndex)] ?: return null
                return child.resolve(path.substring(slashIndex + 1))
            }
        }

        fun add(
            path: String,
            endpoint: Endpoint
        ) {
            val slashIndex = path.indexOf('/')
            if (slashIndex == -1) { // No slash found, last node in tree
                endpoints[path] = endpoint
            } else {
                val child = children.computeIfAbsent(path.substring(0, slashIndex)) { Child() }
                child.add(path.substring(slashIndex + 1), endpoint)
            }
        }
    }
}
