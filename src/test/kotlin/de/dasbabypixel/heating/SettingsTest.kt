package de.dasbabypixel.heating

import de.dasbabypixel.heating.database.Database
import de.dasbabypixel.heating.settings.SettingKey
import de.dasbabypixel.heating.settings.SettingManager
import de.dasbabypixel.heating.settings.SettingType
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SettingsTest {
    @Test
    fun testSettings() {
        val database = mock(Database::class.java)
        `when`(database.settingsByName(anyString())).thenReturn(mapOf("default" to "testValueRet"))

        val clock = Clock.system
        val manager = SettingManager(database, clock)
        val settingKey = SettingKey("keyTest", SettingType.STRING)
        val setting = manager.setting(settingKey)

        assertEquals("testValueRet", setting.value)
        setting.update("test22")
        assertEquals("test22", setting.value)
        setting.update(null)
        assertNull(setting.value)

        assertEquals(manager.setting(settingKey), setting)
    }
}