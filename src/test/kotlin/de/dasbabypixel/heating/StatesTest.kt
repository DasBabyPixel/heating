package de.dasbabypixel.heating

import de.dasbabypixel.heating.database.Database
import de.dasbabypixel.heating.messaging.MessagingService
import org.mockito.Mockito.mock
import kotlin.test.Test

class StatesTest {
    @Test
    fun test() {
        val database = mock(Database::class.java)
        val messagingService = MessagingService()
        val clock = Clock.system
        val stateManager = StateManager(database, messagingService, clock)
        val key1 = StateKey("state1", StateType.STRING)
        val state1 = stateManager.state(key1)
        state1.update("state 1 value")
    }
}