import kotlin.io.path.createDirectories
import kotlin.io.path.notExists

plugins {
    alias(libs.plugins.spring)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.shadow)
    alias(libs.plugins.vaadin)
}

repositories {
    mavenCentral()
}

val standalone = sourceSets.register("standalone")

configurations.named("standaloneImplementation") {
    extendsFrom(configurations.runtimeClasspath.get())
}

dependencies {
    implementation(libs.bundles.logging)
    implementation(libs.bundles.spring) {
        exclude("org.apache.logging.log4j", "log4j-to-slf4j")
        exclude("ch.qos.logback", "logback-classic")
    }
    implementation(libs.vaadin.spring.boot.starter) {
        exclude("org.apache.logging.log4j", "log4j-to-slf4j")
        exclude("ch.qos.logback", "logback-classic")
    }

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    "standaloneImplementation"(sourceSets.main.map { it.output })
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

val createRunDir = tasks.register("createRunDir") {
    val dir = rootProject.projectDir.resolve("run").resolve("gui").toPath()
    outputs.dir(dir)
    doFirst {
        if (dir.notExists()) {
            dir.createDirectories()
        }
    }
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }
    bootRun {
        dependsOn(createRunDir)
        workingDir(createRunDir.map { it.outputs.files.singleFile })
        mainClass = "de.dasbabypixel.heating.gui.MainKt"
        classpath(standalone.map { it.output })
    }
}