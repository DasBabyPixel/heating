package de.dasbabypixel.heating.gui

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.result.view.RedirectView
import reactor.core.publisher.Mono

@Controller
class WebsiteController(
    private val website: LocalWebsite
) {
    @GetMapping("")
    fun defaultPage(): RedirectView {
        return RedirectView("index.html")
    }

    @GetMapping("**")
    fun websitePage(request: ServerHttpRequest): Mono<ResponseEntity<String>> {
        return try {
            val text = website.file(request.path.value())
            Mono.just(ResponseEntity.ok().contentType(type(request.path.value())).body(text))
        } catch (exception: HttpClientErrorException) {
            Mono.error(exception)
        }
    }

    private fun type(string: String): MediaType {
        return if (string.endsWith(".html")) {
            HTML
        } else if (string.endsWith(".js")) {
            JS
        } else {
            OCTET
        }
    }

    private val OCTET = MediaType.APPLICATION_OCTET_STREAM
    private val JS = MediaType("text", "javascript")
    private val HTML = MediaType.TEXT_HTML
}