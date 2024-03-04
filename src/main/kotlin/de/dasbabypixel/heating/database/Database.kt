package de.dasbabypixel.heating.database

import de.dasbabypixel.heating.Setting
import de.dasbabypixel.heating.State
import de.dasbabypixel.heating.sensor.Sensor
import de.dasbabypixel.heating.sensor.SensorEntry
import java.time.Instant

interface Database {
    /**
     * Whether the given setting is known to the database
     */
    fun knowsSetting(
        name: String
    ): Boolean

    /**
     * Query the value of a setting
     */
    fun setting(
        name: String
    ): String?

    /**
     * Set the value of a setting.
     * Use null to remove the value
     */
    fun setting(
        name: String,
        value: String?
    )

    fun logSensor(
        sensor: Sensor,
        entry: SensorEntry
    )

    fun <T> logSettingValue(
        setting: Setting<T>,
        value: T?,
        timestamp: Instant
    )

    fun <T> logStateValue(
        state: State<T>,
        value: T?,
        timestamp: Instant
    )
}
