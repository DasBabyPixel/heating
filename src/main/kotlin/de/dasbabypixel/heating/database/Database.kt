package de.dasbabypixel.heating.database

import de.dasbabypixel.heating.State
import de.dasbabypixel.heating.sensor.Sensor
import de.dasbabypixel.heating.sensor.SensorEntry
import de.dasbabypixel.heating.settings.Setting
import java.time.Instant

interface Database {
    /**
     * Set the value of a setting.
     * Use null to remove the value
     */
    fun setting(
        profile: String,
        name: String,
        value: String?
    )

    fun profilesAndParents(): Map<String, Collection<String>>

    fun parents(
        profile: String,
        parents: Collection<String>
    )

    fun settingsByName(
        name: String
    ): Map<String, String>

    fun logSensor(
        sensor: Sensor,
        entry: SensorEntry
    )

    fun <T> logSettingValue(
        setting: Setting<T>,
        profile: String,
        value: T?,
        timestamp: Instant
    )

    fun <T> logStateValue(
        state: State<T>,
        value: T?,
        timestamp: Instant
    )
}
