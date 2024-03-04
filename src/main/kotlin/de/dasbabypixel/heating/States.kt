package de.dasbabypixel.heating

import de.dasbabypixel.heating.database.Database
import de.dasbabypixel.heating.messaging.Message
import de.dasbabypixel.heating.messaging.MessagingService
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.Objects
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.Volatile

private val logger = LoggerFactory.getLogger("States")

class StateManager(
    private val database: Database,
    private val messagingService: MessagingService,
    private val clock: Clock
) {
    private val states = ConcurrentHashMap<String, State<out Any>>()

    fun <T : Any> state(key: StateKey<T>): State<T> {
        @Suppress("UNCHECKED_CAST") return states.computeIfAbsent(key.name) {
            val state = State(database, clock, messagingService, key)
            val updater = state.updater()
            messagingService.registerListener(key.frequency) { _, message ->
                val deserialized = key.deserializer(message.message)
                updater(deserialized)
            }
            return@computeIfAbsent state
        } as State<T>
    }
}

class State<T>(
    private val database: Database,
    private val clock: Clock,
    private val messagingService: MessagingService,
    val key: StateKey<T>
) {
    @Volatile
    private var stateEntry: StateEntry<T> = StateEntry(null, clock.now())
    private val listeners = CopyOnWriteArrayList<StateUpdateListener<T>>()
    private val updaterCreated = AtomicBoolean(false)

    val entry: StateEntry<T>
        get() = stateEntry
    val value: T?
        get() = entry.value
    val timestamp: Instant
        get() = entry.timestamp

    fun addListener(
        listener: StateUpdateListener<T>
    ) {
        listeners.add(listener)
    }

    fun update(
        value: T?,
        timestamp: Instant
    ) {
        val serialized = key.serializer(value)
        database.logStateValue(this, value, timestamp)
        messagingService.broadcastMessage(key.frequency, Message(UUID.randomUUID(), serialized))
    }

    fun update(
        value: T?
    ) {
        update(value, clock.now())
    }

    internal fun updater(): (T?) -> Unit {
        if (!updaterCreated.compareAndSet(false, true)) {
            throw NoSuchMethodError("State#updater")
        }
        return { // just reuse updaterCreated as the lock object, easiest choice
            synchronized(updaterCreated) {
                val timestamp = clock.now()
                stateEntry = StateEntry(it, timestamp)
                logger.debug("Updated SettingValue of {} to {}", key.name, it)
                for (i in 0 until listeners.size) {
                    listeners[i].update(it)
                }
            }
        }
    }
}

data class StateEntry<T>(
    val value: T?,
    val timestamp: Instant
)

class StateKey<T>(
    val name: String,
    val serializer: (value: T?) -> String?,
    val deserializer: (string: String?) -> T?
) {
    constructor(
        name: String,
        type: StateType<T>
    ) : this(name, type.serializer, type.deserializer)

    // special id to differentiate between state frequency and setting frequency
    val frequency: Int = Objects.hash(name, 15896784)
}

class StateType<T>(
    val serializer: (value: T?) -> String?,
    val deserializer: (string: String?) -> T?
) {
    companion object {
        val STRING = StateType({ it }, { it })
        val DOUBLE = StateType({ it?.toString() }, { it?.toDoubleOrNull() })
        val INT = StateType({ it?.toString() }, { it?.toIntOrNull() })
    }
}

fun interface StateUpdateListener<T> {
    fun update(
        newValue: T?
    )
}