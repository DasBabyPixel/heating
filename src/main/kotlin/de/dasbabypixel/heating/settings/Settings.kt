package de.dasbabypixel.heating.settings

import de.dasbabypixel.heating.Clock
import de.dasbabypixel.heating.database.Database
import org.slf4j.LoggerFactory
import java.util.Objects
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet

private val logger = LoggerFactory.getLogger("Settings")

/**
 * Responsible for managing user-defined settings.
 *
 * These settings make up the configuration of the system and will control functionality.
 */
class SettingManager(
    private val database: Database, // private val messagingService: MessagingService,
    private val clock: Clock
) {
    private val settings = ConcurrentHashMap<String, Setting<out Any>>()
    private val hooks = CopyOnWriteArrayList<(Setting<out Any>) -> (Unit)>()
    private val profiles: MutableMap<String, MutableCollection<String>> = ConcurrentHashMap()
    var activeProfile: String = DEFAULT_PROFILE
        private set

    init {
        profiles[DEFAULT_PROFILE] = CopyOnWriteArraySet()
        val values = database.profilesAndParents()
        values.forEach { (profile, parents) ->
            if (profile == DEFAULT_PROFILE) {
                logger.error("Ignoring invalid setting configuration in database: Configured parent(s) $parents for \"default\" profile. Stop messing with raw database content!")
                return@forEach
            }
            profiles[profile] = CopyOnWriteArraySet(parents.filter {
                if (transitiveParents(it).contains(profile)) {
                    logger.error("Ignoring parent $it for profile $profile to prevent cyclic parenting. Stop messing with raw database content!")
                    return@filter false
                }
                true
            })
        }

        println(profiles)
    }

    private fun transitiveParents(profile: String): Collection<String> {
        val set = HashSet<String>()
        val parents = profiles[profile]
        if (parents != null) {
            if (set.addAll(parents)) {
                parents.forEach {
                    set.addAll(transitiveParents(it))
                }
            }
        }
        return set
    }

    companion object {
        const val DEFAULT_PROFILE = "default"
    }

    fun <T : Any> setting(
        key: SettingKey<T>
    ): Setting<T> {
        @Suppress("UNCHECKED_CAST") return settings.compute(key.name) { name, setting ->
            if (setting != null) {
                if (setting.key !== key) {
                    throw IllegalArgumentException("Trying to query setting $name with different SettingKeys: ${setting.key} and $key")
                }
                return@compute setting
            }

            @Suppress("NAME_SHADOWING")
            val setting = Setting(this, database, clock, key)
            logger.debug("Created Setting {} (defaultValue={})", name, setting.value)

            database.settingsByName(name).apply {
                if (isEmpty()) {
                    setting.update(DEFAULT_PROFILE, setting.key.defaultValue())
                }
            }.forEach { (profile, databaseValue) ->
                val deserialized = key.type.deserializer(databaseValue)
                if (deserialized == null) {
                    logger.error(
                        """
                            Invalid value for Setting $name. Overwriting setting to default value.
                            Value deleted: $databaseValue
                        """.trimIndent()
                    )
                    if (profile == DEFAULT_PROFILE) {
                        setting.update(DEFAULT_PROFILE, setting.key.defaultValue())
                    }
                } else {
                    setting.update(profile, deserialized)
                }
            }
            hooks.forEach { it(setting) }
            return@compute setting
        } as Setting<T>
    }

    fun addHook(function: (Setting<out Any>) -> (Unit)) {
        hooks.add(function)
        allSettings(function)
    }

    fun allSettings(function: (Setting<out Any>) -> (Unit)) {
        settings.values.forEach { function(it) }
    }
}

class Setting<T>(
    val settingManager: SettingManager,
    private val database: Database,
    private val clock: Clock,
    val key: SettingKey<T>
) {
    private val settingValue: SettingValue<T> = SettingValue(this)

    val value: T?
        get() = value(settingManager.activeProfile)

    fun update(value: T?) {
        update(settingManager.activeProfile, value)
    }

    fun value(profile: String): T? {
        return settingValue.value[profile]
    }

    fun update(
        profile: String,
        value: T?
    ) {
        settingValue.update(profile, value)
        val serialized = key.type.serializer(value)
        database.setting(key.name, profile, serialized)
        database.logSettingValue(this, profile, value, clock.now())
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
    private val valueLock = Any()

    val value: MutableMap<String, T?> = ConcurrentHashMap()

    fun addListener(
        listener: SettingValueChangeListener<T>
    ) {
        listeners.add(listener)
    }

    fun update(
        profile: String,
        value: T?
    ) { // just reuse updaterCreated as the lock object, easiest choice
        synchronized(valueLock) {
            if (value == null) this.value.remove(profile)
            else this.value[profile] = value

            logger.debug("Updated SettingValue of {} to {}", setting.key.name, value)
            for (i in 0 until listeners.size) {
                listeners[i].valueChanged(profile, value)
            }
        }
    }
}

fun interface SettingValueChangeListener<T> {
    fun valueChanged(
        profile: String,
        newValue: T?
    )
}

class SettingKey<T>(
    val name: String,
    val type: SettingType<T>,
    val defaultValue: () -> T? = { null },
) {
    companion object    {
        val COOLDOWN_SENSOR_AFTER_PUMP_START = SettingKey("cooldown_sensor_after_pump_start_seconds", SettingType.INT)
        val LEGIONELLA_KILL_INTERVAL_HOURS = SettingKey("legionella_kill_interval_hours", SettingType.INT)
        val LEGIONELLA_KILL_TEMPERATURE_CELSIUS = SettingKey("legionella_kill_temperature_celsius", SettingType.INT)
        val MAX_KETTLE_TEMPERATURE = SettingKey("max_kettle_temperature", SettingType.INT)
        val MIN_KETTLE_TEMPERATURE_TO_PUMP = SettingKey("min_kettle_temperature_to_pump", SettingType.INT)
        val HEATING_ENABLED = SettingKey("heating_enabled", SettingType.BOOLEAN)
        val WARM_WATER_ENABLED = SettingKey("warm_water_enabled", SettingType.BOOLEAN)
    }

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
        val BOOLEAN = SettingType(Boolean::class.java, { it?.toString() }, { it?.toBooleanStrictOrNull() })
    }
}
