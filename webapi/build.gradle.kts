plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
}

group = "de.dasbabypixel.heating"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.mockito:mockito-core:5.23.0")
}

kotlin {
    jvmToolchain(25)
}
