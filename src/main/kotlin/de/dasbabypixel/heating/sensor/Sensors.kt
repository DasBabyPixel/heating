package de.dasbabypixel.heating.sensor

import de.dasbabypixel.heating.StateKey
import de.dasbabypixel.heating.StateManager
import de.dasbabypixel.heating.StateType
import de.dasbabypixel.heating.database.Database
import java.time.Instant

class SensorManager(
    stateManager: StateManager,
    database: Database
) {
    val innen = Sensor(stateManager, database, "innen")
    val aussen = Sensor(stateManager, database, "aussen")
    val kessel = Sensor(stateManager, database, "kessel")
}

data class SensorEntry(
    val value: Double,
    val timestamp: Instant
)

class Sensor(
    private val stateManager: StateManager,
    private val database: Database,
    val name: String
) {
    val state = stateManager.state(StateKey("sensor-$name", StateType.DOUBLE))

    val entry: SensorEntry?
        get() = state.entry.run { value?.let { SensorEntry(it, timestamp) } }

    fun update(
        value: Double,
        timestamp: Instant
    ) {
        val sensorEntry = SensorEntry(value, timestamp)
        database.logSensor(this, sensorEntry)
        state.update(value, timestamp)
    }
}