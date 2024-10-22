import kotlin.io.path.createDirectories
import kotlin.io.path.notExists

plugins {
    id("org.springframework.boot") version "3.2.3"
    kotlin("jvm") version libs.versions.kotlin.get()
    kotlin("plugin.spring") version libs.versions.kotlin.get()
    id("com.github.johnrengelman.shadow") version "8.1.1"
    alias(libs.plugins.vaadin)
    application
}

group = "de.dasbabypixel"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation(libs.bundles.logging)
    // implementation(libs.bundles.netty)
    @Suppress("PackageUpdate") implementation(libs.gson)
    implementation(libs.hikaricp)
    implementation(libs.mysql.connector.j)
    implementation(libs.bundles.spring) {
        exclude("org.apache.logging.log4j", "log4j-to-slf4j")
        exclude("ch.qos.logback", "logback-classic")
    }
    testImplementation(kotlin("test"))
    testImplementation("org.mockito:mockito-core:5.11.0")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
}

gradle.taskGraph.whenReady {
    allTasks.filterIsInstance<JavaExec>().forEach {
        it.setExecutable(it.javaLauncher.get().executablePath.asFile.absolutePath)
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

val createRunDir = tasks.register("createRunDir") {
    val dir = projectDir.resolve("run").toPath()
    doFirst {
        if (dir.notExists()) {
            dir.createDirectories()
        }
    }
}

tasks {
    withType<Test>().configureEach {
        useJUnitPlatform()
    }
    assemble.configure {
        dependsOn(shadowJar)
    }
}

tasks.withType<JavaExec>().configureEach {
    javaLauncher = javaToolchains.launcherFor(java.toolchain)
    standardInput = System.`in`
    standardOutput = System.out
    errorOutput = System.err
}

kotlin {
    jvmToolchain(21)
}

tasks.bootRun.configure {
    dependsOn(createRunDir)
    workingDir("run")
}

application {
    mainClass = "de.dasbabypixel.heating.MainKt"
}
