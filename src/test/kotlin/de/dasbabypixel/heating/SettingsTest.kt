package de.dasbabypixel.heating

import de.dasbabypixel.heating.database.Database
import de.dasbabypixel.heating.messaging.MessagingService
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
        `when`(database.knowsSetting(anyString())).thenReturn(true)
        `when`(database.setting(anyString())).thenReturn("testValueRet")

        val messagingService = MessagingService()
        val clock = Clock.system
        val manager = SettingManager(database, messagingService, clock)
        val settingKey = SettingKey("keyTest", SettingType.STRING)
        val setting = manager.setting(settingKey)

        assertEquals(setting.value, "testValueRet")
        setting.update("test22")
        assertEquals(setting.value, "test22")
        setting.update(null)
        assertNull(setting.value)

        assertEquals(manager.setting(settingKey), setting)
    }
}