package de.dasbabypixel.heating

import de.dasbabypixel.heating.database.Database
import de.dasbabypixel.heating.messaging.Message
import de.dasbabypixel.heating.messaging.MessagingService
import org.slf4j.LoggerFactory
import java.util.Objects
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.Volatile

private val logger = LoggerFactory.getLogger("Settings")

/**
 * Responsible for managing user-defined settings.
 *
 * These settings make up the configuration of the system and will control functionality.
 */
class SettingManager(
    private val database: Database,
    private val messagingService: MessagingService,
    private val clock: Clock
) {
    private val settings = ConcurrentHashMap<String, Setting<out Any>>()

    fun <T : Any> setting(
        key: SettingKey<T>
    ): Setting<T> {
        @Suppress("UNCHECKED_CAST") return settings.compute(key.name) { name, setting ->
            if (setting != null) {
                if (setting.key != key) {
                    throw IllegalArgumentException("Trying to query setting $name with different SettingKeys: ${setting.key} and $key")
                }
                return@compute setting
            }

            @Suppress("NAME_SHADOWING")
            val setting = Setting(database, clock, messagingService, key)
            val value = setting.settingValue
            logger.debug("Created Setting {} (value={})", name, value.value)

            val updater = value.updater()
            if (database.knowsSetting(name)) {
                val databaseValue = database.setting(name)
                val deserialized = key.type.deserializer(databaseValue)
                if (deserialized == null) {
                    logger.error(
                        """
                            Invalid value for Setting $name. Overwriting setting to default value.
                            Value deleted: $databaseValue
                        """.trimIndent()
                    )
                    setDefaultValue(key, updater)
                } else {
                    updater(deserialized)
                }
            } else {
                setDefaultValue(key, updater)
            }
            messagingService.registerListener(key.frequency) { _, message ->
                val deserialized = key.type.deserializer(message.message)
                updater(deserialized)
            }
            return@compute setting
        } as Setting<T>
    }

    private fun <T> setDefaultValue(
        key: SettingKey<T>,
        updater: (T?) -> Unit,
    ) {
        val newValue = key.defaultValue()
        updater(newValue)
        database.setting(key.name, key.type.serializer(newValue))
    }
}

class Setting<T>(
    private val database: Database,
    private val clock: Clock,
    private val messagingService: MessagingService,
    val key: SettingKey<T>
) {
    internal val settingValue: SettingValue<T> = SettingValue(this)

    val value: T?
        get() = settingValue.value

    fun update(
        value: T?
    ) {
        val serialized = key.type.serializer(value)
        database.setting(key.name, serialized)
        database.logSettingValue(this, value, clock.now())
        messagingService.broadcastMessage(
            key.frequency, Message(UUID.randomUUID(), serialized)
        )
    }

    fun addListener(
        listener: SettingValueChangeListener<T>
    ) {
        settingValue.addListener(listener)
    }
}

class SettingValue<T>(
    private val setting: Setting<T>
) {
    private val listeners = CopyOnWriteArrayList<SettingValueChangeListener<T>>()
    private val updaterCreated = AtomicBoolean(false)

    @Volatile
    var value: T? = null
        private set

    fun addListener(
        listener: SettingValueChangeListener<T>
    ) {
        listeners.add(listener)
    }

    internal fun updater(): (T?) -> Unit {
        if (!updaterCreated.compareAndSet(false, true)) {
            throw NoSuchMethodError("SettingValue#updater")
        }
        return { // just reuse updaterCreated as the lock object, easiest choice
            synchronized(updaterCreated) {
                value = it
                logger.debug("Updated SettingValue of {} to {}", setting.key.name, it)
                for (i in 0 until listeners.size) {
                    listeners[i].valueChanged(it)
                }
            }
        }
    }
}

fun interface SettingValueChangeListener<T> {
    fun valueChanged(
        newValue: T?
    )
}

class SettingKey<T>(
    val name: String,
    val type: SettingType<T>,
    val defaultValue: () -> T? = { null },
) {
    // special id to differentiate between state frequency and setting frequency
    val frequency: Int = Objects.hash(name, 89756291)
}

class SettingType<T>(
    val type: Class<T>,
    val serializer: (value: T?) -> String?,
    val deserializer: (string: String?) -> T?,
) {
    companion object {
        val STRING = SettingType(String::class.java, { it }, { it })
        val INT = SettingType(Int::class.java, { it?.toString() }, { it?.toIntOrNull() })
    }
}
