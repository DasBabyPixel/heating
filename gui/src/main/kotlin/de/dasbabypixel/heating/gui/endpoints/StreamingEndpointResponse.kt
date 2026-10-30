package de.dasbabypixel.heating.gui.endpoints

// class StreamingEndpointResponse(val responseCode: Int) : EndpointResponse<StreamingInstance> {
//     override fun respond(httpExchange: HttpExchange): StreamingInstance {
//         httpExchange.exchange.sendResponseHeaders(responseCode, 0)
//         val outputStream = httpExchange.exchange.responseBody
//         return StreamingInstance(httpExchange, outputStream)
//     }
// }
//
// class StreamingInstance(
//     val exchange: HttpExchange,
//     val outputStream: OutputStream
// ) : AutoCloseable {
//     override fun close() {
//         exchange.exchange.close()
//     }
//
//     fun sendMessage() {
//
//     }
// }
