package de.dasbabypixel.heating.gui

import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.RequestPredicates
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.resource.ResourceUrlProvider
import org.springframework.web.reactive.resource.ResourceWebHandler
import reactor.core.publisher.Mono

@Configuration
class SpringConfiguration {
    @Bean
    fun website(): LocalWebsite = LocalWebsite()
}

class LocalWebsite {
    fun file(path: String): String {
        val resource = javaClass.classLoader.getResource("website$path") ?: throw HttpClientErrorException(
            HttpStatusCode.valueOf(404)
        )
        val text = resource.readText()
        return text
    }
}

@Component
@Order(-2)
class ExceptionHandler(
    private val website: LocalWebsite,
    errorAttributes: ErrorAttributes,
    applicationContext: ApplicationContext,
    configurer: ServerCodecConfigurer,
    mapping: ResourceUrlProvider
) : AbstractErrorWebExceptionHandler(errorAttributes, WebProperties.Resources(), applicationContext) {
    init {
        val handler = ResourceWebHandler()
        mapping.registerHandlers(mapOf("/**" to handler))
    }

    override fun getRoutingFunction(
        errorAttributes: ErrorAttributes
    ): RouterFunction<ServerResponse> {
        return RouterFunctions.route(RequestPredicates.all()) { request: ServerRequest ->
            this.renderErrorResponse(request)
        }
    }

    private fun renderErrorResponse(
        request: ServerRequest
    ): Mono<ServerResponse> {
        val error = getError(request)
        if (error != null) {
            if (error is HttpClientErrorException) {
                val status = error.statusCode.value()
                try {
                    val file = website.file("/errors/$status.html")
                    return ServerResponse.badRequest().contentType(MediaType.TEXT_HTML).bodyValue(file)
                } catch (e: HttpClientErrorException) {
                    error.printStackTrace()
                    e.printStackTrace()
                }
            }
        }

        val errorPropertiesMap = getErrorAttributes(
            request, ErrorAttributeOptions.defaults().including(ErrorAttributeOptions.Include.STACK_TRACE)
        )
        return ServerResponse.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue<Map<String, Any>>(errorPropertiesMap))
    }

    init {
        this.setMessageWriters(configurer.writers)
    }
}