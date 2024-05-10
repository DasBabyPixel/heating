package de.dasbabypixel.heating.gui

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api")
class ApiController {
    @GetMapping("apitest")
    fun test(): String {
        return "apitest suc"
    }
}