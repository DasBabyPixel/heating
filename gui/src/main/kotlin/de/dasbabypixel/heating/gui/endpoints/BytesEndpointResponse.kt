package de.dasbabypixel.heating.gui.endpoints

// class BytesEndpointResponse(
//     val statusCode: Int,
//     val bytes: ByteArray
// ) : EndpointResponse<Unit> {
//     override fun respond(httpExchange: HttpExchange) {
//         httpExchange.exchange.sendResponseHeaders(statusCode, bytes.size.toLong())
//         httpExchange.exchange.responseBody.use { it.write(bytes) }
//     }
// }
