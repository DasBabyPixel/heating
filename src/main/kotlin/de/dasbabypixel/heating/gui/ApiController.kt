package de.dasbabypixel.heating.gui

import org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("api")
class ApiController(val websiteController: WebsiteController) {
    @GetMapping("apitest")
    fun test(): String {
        return "apitest suc"
    }

    @GetMapping("register", produces = [TEXT_EVENT_STREAM_VALUE])
    fun register(): Flux<Message> {
        return Flux.create { websiteController.callLogin(it) }
    }
}