package de.dasbabypixel.heating.gui.endpoints

// class EndpointTree(private val root: Node) {
//
//     fun resolve(path: String): Endpoint? {
//         return root.resolve(path)
//     }
//
//     class Builder {
//         private val root = Node()
//
//         class Endpoint {
//             private val byMethod: MutableMap<String, EndpointHandler> = HashMap()
//
//             fun add(
//                 method: String,
//                 handler: EndpointHandler
//             ) {
//                 byMethod[method] = handler
//             }
//
//             fun build(path: String): EndpointTree.Endpoint {
//                 return Endpoint(path, byMethod)
//             }
//         }
//
//         fun build(): EndpointTree {
//             return EndpointTree(root.build())
//         }
//
//         fun add(
//             path: String,
//             method: String,
//             handler: EndpointHandler
//         ) {
//             root.add(path, method, handler)
//         }
//
//         class Node {
//             private val children: MutableMap<String, Node> = HashMap()
//             private val endpoints: MutableMap<String, Endpoint> = HashMap()
//
//             fun build(): EndpointTree.Node {
//                 return Node(
//                     this.children.mapValues { it.value.build() },
//                     this.endpoints.mapValues { it.value.build(it.key) })
//             }
//
//             fun add(
//                 path: String,
//                 method: String,
//                 handler: EndpointHandler
//             ) {
//                 val slashIndex = path.indexOf('/')
//
//                 if (slashIndex == -1) { // No slash found, last node in tree
//                     val endpoint = endpoints.computeIfAbsent(path) { Endpoint() }
//                     endpoint.add(method, handler)
//                 } else {
//                     val child = children.computeIfAbsent(path.substring(0, slashIndex)) { Node() }
//                     child.add(path.substring(slashIndex + 1), method, handler)
//                 }
//             }
//         }
//     }
//
//     class Endpoint(
//         val path: String,
//         private val handlersByMethod: Map<String, EndpointHandler>
//     ) {
//         val supportedMethods: Set<String>
//             get() = handlersByMethod.keys
//
//         fun resolve(method: String): EndpointHandler? {
//             return handlersByMethod[method]
//         }
//     }
//
//     class Node(
//         private val children: Map<String, Node>,
//         private val endpoints: Map<String, Endpoint>
//     ) {
//         fun resolve(path: String): Endpoint? {
//             val slashIndex = path.indexOf('/')
//             if (slashIndex == -1) { // No slash found, last node in tree
//                 return endpoints[path]
//             } else {
//                 val child = children[path.substring(0, slashIndex)] ?: return null
//                 return child.resolve(path.substring(slashIndex + 1))
//             }
//         }
//     }
// }
