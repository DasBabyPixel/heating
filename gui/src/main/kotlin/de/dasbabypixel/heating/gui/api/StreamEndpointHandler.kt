package de.dasbabypixel.heating.gui.api

// class StreamEndpointHandler : EndpointHandler {
//     override fun handle(event: EndpointEvent) {
//         event.httpExchange.setHeader("Content-Type", "text/event-stream")
//         event.httpExchange.setHeader("Cache-Control", "no-cache")
//         event.httpExchange.setHeader("Connection", "keep-alive")
//
//         event.httpExchange.exchange.sendResponseHeaders(200, 0)
//         val writer = event.httpExchange.exchange.responseBody.bufferedWriter()
//         try {
//             for (i in 0 until 60) {
//                 val payload = "{\"count\": $i, \"time\": \"${LocalTime.now()}\"}"
//                 val sseMessage = "data: $payload\n\n"
//                 writer.write(sseMessage)
//                 writer.flush()
//                 Thread.sleep(1000)
//             }
//             writer.close()
//         } catch (e: Exception) {
//             e.printStackTrace()
//         } finally {
//             event.httpExchange.exchange.close()
//         }
//     }
// }
