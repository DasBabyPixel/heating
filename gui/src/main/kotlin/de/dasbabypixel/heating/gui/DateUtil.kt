package de.dasbabypixel.heating.gui

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.util.Locale

object DateUtil {
    fun format(temporal: TemporalAccessor): String {
        return DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
            .withZone(ZoneId.of("GMT"))
            .format(temporal)
    }
}
