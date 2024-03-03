plugins {
    kotlin("jvm") version "1.9.22"
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
    testImplementation(kotlin("test"))
    testImplementation("org.mockito:mockito-core:5.10.0")
}

tasks {
    test {
        useJUnitPlatform()
    }
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass = "MainKt"
}
