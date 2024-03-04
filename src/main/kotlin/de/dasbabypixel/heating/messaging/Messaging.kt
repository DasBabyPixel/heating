package de.dasbabypixel.heating.messaging

import org.slf4j.LoggerFactory
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

private val logger = LoggerFactory.getLogger("Messaging")

class MessagingService {
    private val listeners: MutableMap<Int, MutableList<MessageListener>> = ConcurrentHashMap()

    @OptIn(ExperimentalStdlibApi::class)
    internal fun receiveMessage(
        frequency: Int,
        message: Message
    ) {
        logger.debug("Received message on frequency {}: {}", frequency.toHexString(HexFormat.UpperCase), message)

        val messageListeners = listeners[frequency] ?: return
        for (messageListener in messageListeners) {
            messageListener.receiveUpdate(frequency, message)
        }
    }

    /**
     * Broadcasts a message to the entire network.
     *
     * The message will reach the current program, as well as every program connected to this MessagingService
     */
    fun broadcastMessage(
        frequency: Int,
        message: Message
    ) {
        receiveMessage(frequency, message)
    }

    fun registerListener(
        frequency: Int,
        listener: MessageListener
    ) {
        listeners.computeIfAbsent(frequency) { _ -> CopyOnWriteArrayList() }.add(listener)
    }
}

data class Message(
    val id: UUID,
    val message: String?
)

fun interface MessageListener {
    fun receiveUpdate(
        frequency: Int,
        message: Message
    )
}