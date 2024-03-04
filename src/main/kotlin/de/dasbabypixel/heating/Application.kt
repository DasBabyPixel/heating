package de.dasbabypixel.heating

import de.dasbabypixel.heating.database.Database
import de.dasbabypixel.heating.messaging.MessagingService

class Application(
    val clock: Clock,
    val messagingService: MessagingService,
    val database: Database,
    val settingManager: SettingManager,
    val stateManager: StateManager
)