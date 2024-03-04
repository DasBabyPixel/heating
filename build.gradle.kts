plugins {
    id("org.springframework.boot") version "3.2.3"
    kotlin("jvm") version libs.versions.kotlin.get()
    kotlin("plugin.spring") version libs.versions.kotlin.get()
    application
}

group = "de.dasbabypixel"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.logging)
    implementation(libs.bundles.netty)
    implementation(libs.gson)
    implementation(libs.hikaricp)
    implementation(libs.mysql.connector.j)
    implementation(libs.bundles.spring) {
        exclude("org.apache.logging.log4j", "log4j-to-slf4j")
        exclude("ch.qos.logback", "logback-classic")
    }
    testImplementation(kotlin("test"))
    testImplementation("org.mockito:mockito-core:5.10.0")
}

tasks {
    withType<Test>().configureEach {
        useJUnitPlatform()
    }
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass = "de.dasbabypixel.heating.MainKt"
}
