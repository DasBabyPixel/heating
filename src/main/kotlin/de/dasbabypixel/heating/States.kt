package de.dasbabypixel.heating

import de.dasbabypixel.heating.database.Database
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.Objects
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.Volatile

private val logger = LoggerFactory.getLogger("States")

class StateManager(
    private val database: Database,
    private val clock: Clock
) {
    private val states = ConcurrentHashMap<String, State<out Any>>()
    private val hooks = CopyOnWriteArrayList<(State<out Any>) -> (Unit)>()

    fun <T : Any> state(key: StateKey<T>): State<T> {
        @Suppress("UNCHECKED_CAST") return states.computeIfAbsent(key.name) {
            val state = State(
                database, clock, key
            ) // val updater =  //     state.updater() // messagingService.registerListener(key.frequency) { _, message -> //     val deserialized = key.deserializer(message.message) //     updater(deserialized) // }
            hooks.forEach { it(state) }
            return@computeIfAbsent state
        } as State<T>
    }

    /**
     * Adds a hook into all existing and all newly created states. The hook cannot be removed
     */
    fun addHook(function: (State<out Any>) -> (Unit)) {
        hooks.add(function)
        allStates(function)
    }

    fun allStates(function: (State<out Any>) -> (Unit)) {
        states.values.forEach { function(it) }
    }
}

class State<T>(
    private val database: Database,
    private val clock: Clock,
    val key: StateKey<T>
) {
    @Volatile
    private var stateEntry: StateEntry<T> = StateEntry(null, clock.now())
    private val listeners = CopyOnWriteArrayList<StateUpdateListener<in T>>()
    private val updaterCreated = AtomicBoolean(false)

    val entry: StateEntry<T>
        get() = stateEntry
    val value: T?
        get() = entry.value
    val timestamp: Instant
        get() = entry.timestamp

    fun addListener(
        listener: StateUpdateListener<in T>
    ) {
        listeners.add(listener)
    }

    fun update(
        value: T?,
        timestamp: Instant
    ) {
        synchronized(updaterCreated) {
            stateEntry = StateEntry(value, timestamp)
            logger.debug("Updated SettingValue of {} to {}", key.name, value)
            for (i in 0 until listeners.size) {
                listeners[i].update(value)
            }
        }
        database.logStateValue(
            this, value, timestamp
        )
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
        return ::update
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

    init {
        if (name.lowercase() != name) {
            throw IllegalArgumentException("Name must be lowercase, namespaced recommended")
        }
    }

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