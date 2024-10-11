package de.dasbabypixel.heating.gui

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.FluxSink
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicInteger

class User(
    val id: Int,
    val token: String,
    val sink: FluxSink<Message>
) : MessageReceiver {
    override fun receive(message: Message) {
        sink.next(message)
    }
}

class UserManager : MessageReceiver {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(UserManager::class.java)
    }

    private val id = AtomicInteger()
    private val users = CopyOnWriteArraySet<User>()
    private val userByToken = ConcurrentHashMap<String, User>()
    private val userById = ConcurrentHashMap<Int, User>()

    fun createUser(sink: FluxSink<Message>): User {
        val id = this.id.incrementAndGet()
        val token = generateToken(id)
        val user = User(id, token, sink)
        users.add(user)
        userByToken[token] = user
        userById[id] = user
        logger.debug("User with ID $id created")
        return user
    }

    fun deleteUser(user: User) {
        if (users.remove(user)) {
            userByToken.remove(user.token)
            userById.remove(user.id)
            logger.debug("User with ID ${user.id} logged out")
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun generateToken(id: Int): String {
        return id.toHexString(HexFormat.UpperCase)
    }

    override fun receive(message: Message) {
        for (user in users) {
            user.receive(message)
        }
    }
}