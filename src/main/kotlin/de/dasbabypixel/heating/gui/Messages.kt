package de.dasbabypixel.heating.gui

import java.time.Instant

interface MessageReceiver {
    fun receive(message: Message)
}

interface Message {
    val type: String
}

class UserLogin(
    user: User,
) : Message {
    val userId = user.id
    override val type = "user_login"
}

class StateValue(
    val stateName: String,
    val value: String,
    val timestamp: Instant,
    val frequency: Int
) : Message {
    override val type = "state_value"
}

class SensorValue(
    val sensorName: String,
    val value: Double,
    val timestamp: Instant
) : Message {
    override val type = "sensor_value"
}
