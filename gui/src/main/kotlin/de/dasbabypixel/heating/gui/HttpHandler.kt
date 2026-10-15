package de.dasbabypixel.heating.gui

fun interface HttpHandler {
    fun handle(exchange: HttpExchange)
}
