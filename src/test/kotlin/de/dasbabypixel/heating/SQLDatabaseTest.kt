package de.dasbabypixel.heating

import de.dasbabypixel.heating.config.JsonConfiguration
import de.dasbabypixel.heating.database.SqlDatabase
import de.dasbabypixel.heating.sensor.Sensor
import de.dasbabypixel.heating.sensor.SensorEntry
import de.dasbabypixel.heating.settings.SettingKey
import de.dasbabypixel.heating.settings.SettingManager
import de.dasbabypixel.heating.settings.SettingType
import java.time.Instant
import kotlin.test.Test

class SQLDatabaseTest {
    @Test
    fun test() {
        val database = SqlDatabase(JsonConfiguration(javaClass.classLoader.getResource("mysql.json")!!.readText()))
        val clock = Clock.system
        val settingManager = SettingManager(database, clock)
        val stateManager = StateManager(database, clock)
        val sensor = Sensor(stateManager, database, "innen")
        val entry = SensorEntry(15.4, Instant.now())
        database.logSensor(sensor, entry)

        val settingKey1 = SettingKey("test_setting_1", SettingType.STRING)
        val settingKey2 = SettingKey("test_setting_2", SettingType.INT)
        val stateKey1 = StateKey("test_state_1", StateType.STRING)
        val stateKey2 = StateKey("test_state_2", StateType.INT)

        val setting1 = settingManager.setting(settingKey1)
        val setting2 = settingManager.setting(settingKey2)
        val state1 = stateManager.state(stateKey1)
        val state2 = stateManager.state(stateKey2)

        setting1.update("test 1")
        setting1.update("test 2")
        setting2.update(1)
        setting1.update("test 3")
        setting2.update(2)
        setting2.update(3)
        state1.update("state 1")
        state1.update("state 2")
        state2.update(1)
        state1.update("state 3")
        state2.update(2)
        state2.update(3)
    }
}