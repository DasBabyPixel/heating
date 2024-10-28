package de.dasbabypixel.heating.gui

import de.dasbabypixel.heating.Application
import de.dasbabypixel.heating.State
import de.dasbabypixel.heating.StateEntry
import de.dasbabypixel.heating.settings.Setting
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import reactor.core.publisher.FluxSink
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

@Controller
class WebsiteController(
    private val website: LocalWebsite,
    private val application: Application
) {

    private val users = UserManager()
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor {
        Thread(it).apply {
            isDaemon = true
        }
    }

    init {
        for (i in 0 until 100) {
            println(1)
        }
        application.stateManager.addHook { state ->
            state.addListener { _ ->
                val update = createStateUpdate(state)
                executor.execute { users.receive(update) }
            }
            val update = createStateUpdate(state)
            executor.execute { users.receive(update) }
        }
        @Suppress("UNCHECKED_CAST") application.settingManager.addHook { setting ->
            (setting as Setting<Any>).addListener { profile, newValue ->
                val update = createSettingUpdate(setting, profile, newValue)
                executor.execute { users.receive(update) }
            }
            val update = createSettingUpdate(setting)
            executor.execute { users.receive(update) }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun createSettingUpdate(
        setting: Setting<*>,
        profile: String = setting.settingManager.activeProfile,
        value: Any? = setting.value(profile)
    ): SettingValue {
        return SettingValue(
            setting.key.name, profile, (setting.key.type.serializer as ((value: Any?) -> String))(value)
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun createStateUpdate(
        state: State<*>,
        stateEntry: StateEntry<out Any?> = state.entry
    ): StateValue {
        @Suppress("UNCHECKED_CAST")
        val serialized = (state.key.serializer as ((value: Any?) -> String))(stateEntry.value)
        return StateValue(state.key.name, serialized, stateEntry.timestamp, state.key.frequency)
    }

    fun callLogin(sink: FluxSink<Message>): User {
        println("Login")
        val user = users.createUser(sink)
        executor.execute {
            users.receive(UserLogin(user))
            application.stateManager.allStates {
                val update = createStateUpdate(it)
                user.receive(update)
            }
            application.settingManager.allSettings {
                val update = createSettingUpdate(it)
                user.receive(update)
            }
        }
        sink.onCancel {
            println("Logout")
            callLogout(user)
        }
        sink.onDispose {
            println("Dispose")
        }
        return user
    }

    private fun callLogout(user: User) {
        users.deleteUser(user)
    }

    //
    // @GetMapping("")
    // fun defaultPage(): RedirectView {
    //     return RedirectView("index.html")
    // }
    //
    // @GetMapping("**")
    // fun websitePage(request: ServerHttpRequest): Mono<ResponseEntity<ByteArray>> {
    //     return try {
    //         val path = request.path.value()
    //         val text = if (path.endsWith("/")) website.file(path + "index.html") else website.file(path)
    //         Mono.just(ResponseEntity.ok().contentType(type(path)).body(text))
    //     } catch (exception: HttpClientErrorException) {
    //         Mono.error(exception)
    //     }
    // }

    private fun type(string: String): MediaType {
        return if (string.endsWith(".html")) {
            html
        } else if (string.endsWith(".js")) {
            js
        } else if (string.endsWith(".css")) {
            css
        } else if (string.endsWith(".png")) {
            png
        } else if (string.endsWith(".ico")) {
            ico
        } else if (string.endsWith("/")) {
            html
        } else {
            octet
        }
    }

    companion object {
        private val octet = MediaType.APPLICATION_OCTET_STREAM
        private val js = MediaType("text", "javascript")
        private val css = MediaType("text", "css")
        private val html = MediaType.TEXT_HTML
        private val png = MediaType("image", "png")
        private val ico = MediaType("image", "x-icon")
    }
}