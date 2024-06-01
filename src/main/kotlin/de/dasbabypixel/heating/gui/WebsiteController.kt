package de.dasbabypixel.heating.gui

import de.dasbabypixel.heating.Application
import de.dasbabypixel.heating.State
import de.dasbabypixel.heating.StateKey
import de.dasbabypixel.heating.StateType
import de.dasbabypixel.heating.StateUpdateListener
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.reactive.result.view.RedirectView
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit.SECONDS

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
        application.stateManager.allStates { state ->
            println("Found State ${state.key.name} with value ${state.value}")
            val l: StateUpdateListener<in Any> = StateUpdateListener { _ ->
                val stateEntry = state.entry
                val timestamp = stateEntry.timestamp
                val value = stateEntry.value

                @Suppress("UNCHECKED_CAST")
                val serialized = (state.key.serializer as ((value: Any?) -> String))(value)

                executor.execute {
                    users.receive(StateValue(state.key.name, serialized, timestamp, state.key.frequency))
                }

            }
            state.addListener(listener = l)
        }
        val stateKey = StateKey("test_state", StateType.DOUBLE)
        val state: State<Double> = application.stateManager.state(stateKey)
        state.update(10.toDouble())
        executor.scheduleAtFixedRate({ // if (Math.random() < 0.01) {
            //     state.update(state.value?.plus(1))
            // }
        }, 1, 1, SECONDS)
    }

    private fun callLogin(sink: FluxSink<Message>): User {
        val user = users.createUser(sink)
        executor.execute {
            users.receive(UserLogin(user))
            application.stateManager.allStates { state ->
                val stateEntry = state.entry

                @Suppress("UNCHECKED_CAST")
                val serialized = (state.key.serializer as ((value: Any?) -> String))(stateEntry.value)
                user.receive(StateValue(state.key.name, serialized, stateEntry.timestamp, state.key.frequency))
            }
        }
        sink.onCancel { callLogout(user) }
        return user
    }

    private fun callLogout(user: User) {
        users.deleteUser(user)
    }

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

    @ResponseBody
    @GetMapping("/register", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun register(): Flux<Message> {
        return Flux.create { callLogin(it) }
    }

    private fun type(string: String): MediaType {
        return if (string.endsWith(".html")) {
            html
        } else if (string.endsWith(".js")) {
            js
        } else if (string.endsWith(".css")) {
            css
        } else {
            octet
        }
    }

    companion object {
        private val octet = MediaType.APPLICATION_OCTET_STREAM
        private val js = MediaType("text", "javascript")
        private val css = MediaType("text", "css")
        private val html = MediaType.TEXT_HTML
    }
}