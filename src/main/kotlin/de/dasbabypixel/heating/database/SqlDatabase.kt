package de.dasbabypixel.heating.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import de.dasbabypixel.heating.State
import de.dasbabypixel.heating.config.Configuration
import de.dasbabypixel.heating.sensor.Sensor
import de.dasbabypixel.heating.sensor.SensorEntry
import de.dasbabypixel.heating.settings.Setting
import java.sql.PreparedStatement
import java.sql.Timestamp
import java.time.Instant

class SqlDatabase(
    username: String,
    password: String,
    hostname: String,
    port: Int,
    database: String
) : Database {

    constructor(configuration: Configuration) : this(
        configuration.getString("username")!!,
        configuration.getString("password")!!,
        configuration.getString("hostname")!!,
        configuration.getInt("port")!!,
        configuration.getString("database")!!
    )

    private val dataSource: HikariDataSource

    init {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:mysql://$hostname:$port/$database"
        config.username = username
        config.password = password
        config.minimumIdle = 1

        dataSource = HikariDataSource(config)
        dataSource.connection.use { it ->
            it.createStatement()
                .use { it.execute("CREATE TABLE IF NOT EXISTS `settings` (`name` VARCHAR(64) NOT NULL, `profile` TEXT NOT NULL, `value` TEXT NULL, PRIMARY KEY (`name`)) ENGINE = InnoDB") }
            it.createStatement()
                .use { it.execute("CREATE TABLE IF NOT EXISTS `profiles` (`name` VARCHAR(256) NOT NULL, `parent` VARCHAR(256) NOT NULL, UNIQUE (`name`, `parent`)) ENGINE = InnoDB") }
        }
    }

    override fun settingsByName(name: String): Map<String, String> {
        return dataSource.connection.use { connection ->
            connection.prepareStatement("SELECT `profile`,`value` FROM `settings` WHERE `name`=?").use { statement ->
                statement.setString(1, name)
                statement.executeQuery().use { resultSet ->
                    val map = HashMap<String, String>()
                    while (resultSet.next()) {
                        val profile = resultSet.getString("profile")
                        val value = resultSet.getString("value")
                        map[profile] = value
                    }
                    map
                }
            }
        }
    }

    override fun setting(
        profile: String,
        name: String,
        value: String?
    ) {
        dataSource.connection.use { connection ->
            connection.prepareStatement("INSERT INTO `settings` (`name`, `profile`, `value`) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE `value`=?")
                .use { statement ->
                    statement.setString(1, name)
                    statement.setString(2, profile)
                    statement.setString(3, value)
                    statement.setString(4, value)
                    statement.execute()
                }
        }
    }

    override fun profilesAndParents(): Map<String, Collection<String>> {
        return dataSource.connection.use { connection ->
            connection.prepareStatement("SELECT `name`,`parent` FROM `profiles`").use { statement ->
                statement.executeQuery().use { resultSet ->
                    val map = HashMap<String, MutableCollection<String>>()
                    while (resultSet.next()) {
                        val name = resultSet.getString("name")
                        val parent = resultSet.getString("parent")
                        map.computeIfAbsent(name) { HashSet() }.add(parent)
                    }
                    map
                }
            }
        }
    }

    override fun parents(
        profile: String,
        parents: Collection<String>
    ) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            connection.prepareStatement("DELETE FROM `profiles` WHERE `name`=?").use { statement ->
                statement.setString(1, profile)
                statement.execute()
            }
            if (parents.isNotEmpty()) {
                connection.prepareStatement("INSERT INTO `profiles` (`name`, `parent`) VALUES (?, ?)").use {
                    parents.forEach { parent ->
                        it.setString(1, profile)
                        it.setString(2, parent)
                        it.addBatch()
                    }
                    it.executeBatch()
                }
            }
            connection.commit()
        }
    }

    override fun logSensor(
        sensor: Sensor,
        entry: SensorEntry
    ) {
        log(
            "sensor_${sensor.name}",
            "(`id`, `value`, `timestamp`) VALUES (NULL, ?, ?)",
            {
                it.setString(1, entry.value.toString())
                it.setTimestamp(2, Timestamp.from(entry.timestamp))
            },
            "(`id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, `value` DOUBLE NOT NULL, `timestamp` TIMESTAMP(6) NOT NULL, PRIMARY KEY(`id`))",
            {})
    }

    override fun <T> logSettingValue(
        setting: Setting<T>,
        profile: String,
        value: T?,
        timestamp: Instant
    ) {
        log(
            "setting_${setting.key.name}",
            "(`id`, `profile`, `value`, `timestamp`) VALUES (NULL, ?, ?, ?)",
            {
                it.setString(1, profile)
                it.setString(2, if (value == null) null else setting.key.type.serializer(value))
                it.setTimestamp(3, Timestamp.from(timestamp))
            },
            "(`id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, `profile` VARCHAR(256) NOT NULL, `value` TEXT NULL, `timestamp` TIMESTAMP(6) NOT NULL, PRIMARY KEY(`id`))",
            {})
    }

    override fun <T> logStateValue(
        state: State<T>,
        value: T?,
        timestamp: Instant
    ) {
        log(
            "state_${state.key.name}",
            "(`id`, `value`, `timestamp`) VALUES (NULL, ?, ?)",
            {
                it.setString(1, if (value == null) null else state.key.serializer(value))
                it.setTimestamp(2, Timestamp.from(timestamp))
            },
            "(`id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, `value` TEXT NULL, `timestamp` TIMESTAMP(6) NOT NULL, PRIMARY KEY(`id`))",
            {})
    }

    private inline fun log(
        name: String,
        sqlInsert: String,
        sqlInsertConfiguration: (PreparedStatement) -> Unit,
        sqlCreate: String,
        sqlCreateConfiguration: (PreparedStatement) -> Unit,
    ) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS `log_$name` $sqlCreate ENGINE = InnoDB").use {
                sqlCreateConfiguration(it)
                it.execute()
            }
            connection.prepareStatement("INSERT INTO `log_$name` $sqlInsert").use {
                sqlInsertConfiguration(it)
                it.execute()
            }
            connection.commit()
        }
    }
}