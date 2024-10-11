package de.dasbabypixel.heating.gui

import com.google.gson.JsonObject
import de.dasbabypixel.heating.Application
import de.dasbabypixel.heating.Clock
import de.dasbabypixel.heating.SettingManager
import de.dasbabypixel.heating.StateManager
import de.dasbabypixel.heating.config.JsonConfiguration
import de.dasbabypixel.heating.database.Database
import de.dasbabypixel.heating.database.SqlDatabase
import de.dasbabypixel.heating.messaging.MessagingService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
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
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createFile
import kotlin.io.path.notExists
import kotlin.io.path.readText
import kotlin.io.path.writeText

@Configuration
class SpringConfiguration {
    @Bean
    fun website(): LocalWebsite {
        return LocalWebsite()
    }

    @Bean
    fun clock(): Clock {
        return Clock.system
    }

    @Bean
    fun messagingService(): MessagingService {
        return MessagingService()
    }

    @Bean
    fun mysqlConfiguration(): de.dasbabypixel.heating.config.Configuration {
        val configFile: Path = Paths.get("mysql.json")
        if (configFile.notExists()) {
            configFile.createFile().writeText(JsonConfiguration.gson.toJson(JsonObject().apply {
                addProperty("username", "root")
                addProperty("password", "")
                addProperty("port", 3306)
                addProperty("hostname", "localhost")
                addProperty("database", "heating")
            }))
        }

        val jsonObject: JsonObject = JsonConfiguration.gson.fromJson(configFile.readText(), JsonObject::class.java)
        return JsonConfiguration(jsonObject)
    }

    @Bean
    fun database(): Database {
        return SqlDatabase(mysqlConfiguration())
    }

    @Bean
    fun settingsManager(): SettingManager {
        return SettingManager(database(), messagingService(), clock())
    }

    @Bean
    fun stateManager(): StateManager {
        return StateManager(database(), messagingService(), clock())
    }

    @Bean
    fun application(): Application {
        return Application(
            clock(), messagingService(), database(), settingsManager(), stateManager()
        )
    }
}

class LocalWebsite {
    @EventListener(ApplicationReadyEvent::class)
    fun test() {
        LoggerFactory.getLogger(javaClass).info("Application Ready")
    }

    fun file(path: String): ByteArray {
        val resource = javaClass.classLoader.getResource("website$path") ?: throw HttpClientErrorException(
            HttpStatusCode.valueOf(404)
        )
        return resource.readBytes()
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
        return ServerResponse.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue<Map<String, Any>>(errorPropertiesMap))
    }

    init {
        this.setMessageWriters(configurer.writers)
    }
}