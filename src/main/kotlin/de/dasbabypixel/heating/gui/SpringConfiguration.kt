package de.dasbabypixel.heating.gui

import com.google.gson.JsonObject
import de.dasbabypixel.heating.Application
import de.dasbabypixel.heating.Clock
import de.dasbabypixel.heating.StateManager
import de.dasbabypixel.heating.config.JsonConfiguration
import de.dasbabypixel.heating.database.Database
import de.dasbabypixel.heating.database.SqlDatabase
import de.dasbabypixel.heating.settings.SettingManager
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.http.HttpStatusCode
import org.springframework.web.client.HttpClientErrorException
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

    // @Bean
    // fun messagingService(): MessagingService {
    //     return MessagingService()
    // }

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
        return SettingManager(database(), clock())
    }

    @Bean
    fun stateManager(): StateManager {
        return StateManager(database(), clock())
    }

    @Bean
    fun application(): Application {
        return Application(
            clock(), database(), settingsManager(), stateManager()
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

// @Order(-2)
// @SpringComponent
// class ExceptionHandler(
//     private val website: LocalWebsite,
//     errorAttributes: ErrorAttributes,
//     applicationContext: ApplicationContext,
//     configurer: ServerCodecConfigurer,
//     mapping: ResourceUrlProvider
// ) : AbstractErrorWebExceptionHandler(errorAttributes, WebProperties.Resources(), applicationContext) {
//     init {
//         val handler = ResourceWebHandler()
//         mapping.registerHandlers(mapOf("/**" to handler))
//     }
//
//     override fun getRoutingFunction(
//         errorAttributes: ErrorAttributes
//     ): RouterFunction<ServerResponse> {
//         return RouterFunctions.route(RequestPredicates.all()) { request: ServerRequest ->
//             this.renderErrorResponse(request)
//         }
//     }
//
//     private fun renderErrorResponse(
//         request: ServerRequest
//     ): Mono<ServerResponse> {
//         val error = getError(request)
//         if (error != null) {
//             if (error is HttpClientErrorException) {
//                 val status = error.statusCode.value()
//                 try {
//                     val file = website.file("/errors/$status.html")
//                     return ServerResponse.badRequest().contentType(MediaType.TEXT_HTML).bodyValue(file)
//                 } catch (e: HttpClientErrorException) {
//                     error.printStackTrace()
//                     e.printStackTrace()
//                 }
//             }
//         }
//
//         val errorPropertiesMap = getErrorAttributes(
//             request, ErrorAttributeOptions.defaults().including(ErrorAttributeOptions.Include.STACK_TRACE)
//         )
//         return ServerResponse.status(HttpStatus.BAD_REQUEST)
//             .contentType(MediaType.APPLICATION_JSON)
//             .body(BodyInserters.fromValue<Map<String, Any>>(errorPropertiesMap))
//     }
//
//     init {
//         this.setMessageWriters(configurer.writers)
//     }
// }