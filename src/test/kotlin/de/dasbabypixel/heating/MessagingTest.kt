package de.dasbabypixel.heating

import de.dasbabypixel.heating.messaging.Message
import de.dasbabypixel.heating.messaging.MessagingService
import org.junit.jupiter.api.Assertions.assertEquals
import java.util.UUID
import kotlin.test.Test

internal class MessagingTest {
    @Test
    fun testMessagingService() {
        val messagingService = MessagingService()
        val message = Message(UUID(3198764, 148269813), "dummy")
        val frequency = 21980744
        messagingService.registerListener(frequency) { f, m ->
            assertEquals(f, frequency)
            assertEquals(m, message)
        }
        messagingService.receiveMessage(frequency, message)
    }
}