package de.dasbabypixel.heating.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import de.dasbabypixel.heating.Setting
import de.dasbabypixel.heating.State
import de.dasbabypixel.heating.config.Configuration
import de.dasbabypixel.heating.sensor.Sensor
import de.dasbabypixel.heating.sensor.SensorEntry
import java.sql.Timestamp
import java.time.Instant

class SqlDatabase(
    private val username: String,
    private val password: String,
    private val hostname: String,
    private val port: Int,
    private val database: String
) : Database {

    constructor(
        configuration: Configuration
    ) : this(
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

        dataSource = HikariDataSource(config)
        dataSource.connection.use { it ->
            it.prepareStatement("CREATE TABLE IF NOT EXISTS `settings` (`name` VARCHAR(64) NOT NULL, `value` TEXT NULL, PRIMARY KEY (`name`)) ENGINE = InnoDB")
                .use { it.execute() }
            it.prepareStatement("CREATE TABLE IF NOT EXISTS `log_states` (`id` BIGINT NOT NULL AUTO_INCREMENT, `name` VARCHAR(64) NOT NULL, `value` TEXT NULL, `timestamp` TIMESTAMP(6) NOT NULL, PRIMARY KEY(`id`)) ENGINE = InnoDB")
                .use { it.execute() }
            it.prepareStatement("CREATE TABLE IF NOT EXISTS `log_settings` (`id` BIGINT NOT NULL AUTO_INCREMENT, `name` VARCHAR(64) NOT NULL, `value` TEXT NULL, `timestamp` TIMESTAMP(6) NOT NULL, PRIMARY KEY(`id`)) ENGINE = InnoDB")
                .use { it.execute() }
        }
    }

    override fun knowsSetting(
        name: String
    ): Boolean {
        return dataSource.connection.use { connection ->
            connection.prepareStatement("SELECT EXISTS(SELECT * FROM `settings` WHERE `name`='$name')")
                .use { statement ->
                    statement.executeQuery().use { resultSet ->
                        check(resultSet.next())
                        resultSet.getInt(1) == 1
                    }
                }
        }
    }

    override fun setting(
        name: String
    ): String? {
        return dataSource.connection.use { connection ->
            connection.prepareStatement("SELECT `value` FROM `settings` WHERE `name`='$name'").use { statement ->
                statement.executeQuery().use { resultSet ->
                    check(resultSet.next())
                    resultSet.getString("value")
                }
            }
        }
    }

    override fun setting(
        name: String,
        value: String?
    ) {
        val sqlVal = if (value == null) "NULL" else "'$value'"
        dataSource.connection.use { connection ->
            connection.prepareStatement("INSERT INTO `settings` (`name`, `value`) VALUES ('$name', $sqlVal) ON DUPLICATE KEY UPDATE `name`='$name', `value`=$sqlVal")
                .use { statement -> statement.execute() }
        }
    }

    override fun logSensor(
        sensor: Sensor,
        entry: SensorEntry
    ) {
        dataSource.connection.use { connection ->
            connection.autoCommit = false
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS `log_sensor_${sensor.name}` (`id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, `value` DOUBLE NOT NULL, `timestamp` TIMESTAMP(6) NOT NULL, PRIMARY KEY(`id`)) ENGINE = InnoDB")
                .use { it.execute() }
            connection.prepareStatement(
                "INSERT INTO `log_sensor_${sensor.name}` (`id`, `value`, `timestamp`) VALUES (NULL, '${entry.value}', ' ${
                    Timestamp.from(
                        entry.timestamp
                    )
                }')"
            ).use { it.execute() }
            connection.commit()
        }
    }

    override fun <T> logSettingValue(
        setting: Setting<T>,
        value: T?,
        timestamp: Instant
    ) {
        val sqlVal = if (value == null) "NULL" else "'${setting.key.type.serializer(value)}'"
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                "INSERT INTO `log_settings` (`id`, `name`, `value`, `timestamp`) VALUES (NULL, '${setting.key.name}', $sqlVal, '${
                    Timestamp.from(timestamp)
                }')"
            ).use { it.execute() }
        }
    }

    override fun <T> logStateValue(
        state: State<T>,
        value: T?,
        timestamp: Instant
    ) {
        val sqlVal = if (value == null) "NULL" else "'${state.key.serializer(value)}'"
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                "INSERT INTO `log_states` (`id`, `name`, `value`, `timestamp`) VALUES (NULL, '${state.key.name}', $sqlVal, '${
                    Timestamp.from(timestamp)
                }')"
            ).use { it.execute() }
        }
    }
}