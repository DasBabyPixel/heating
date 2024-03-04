@file:JvmMultifileClass @file:JvmName("StandardKt")

package de.dasbabypixel.heating

import java.time.Duration
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.MILLISECONDS

abstract class Clock {
    companion object {
        val system: Clock = SystemClock()
    }

    abstract fun now(): Instant

    abstract fun futureFor(instant: Instant): CompletableFuture<Void>
}

fun now(clock: Clock): Instant {
    return clock.now()
}

private class SystemClock : Clock() {
    override fun now(): Instant {
        return Instant.now(java.time.Clock.systemDefaultZone())
    }

    override fun futureFor(instant: Instant): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        Executors.newSingleThreadScheduledExecutor().run {
            val duration = Duration.between(instant, now())
            schedule({
                future.complete(null)
            }, duration.toMillis(), MILLISECONDS)
            shutdown()
        }
        return future
    }
}