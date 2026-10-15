import kotlin.io.path.createDirectories
import kotlin.io.path.notExists

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

kotlin {
    jvmToolchain(25)
}

val standalone = sourceSets.register("standalone")

configurations.named("standaloneImplementation") {
    extendsFrom(configurations.runtimeClasspath.get())
}

dependencies {
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation(libs.bundles.logging) // implementation(libs.bundles.spring) {
    //     exclude("org.apache.logging.log4j", "log4j-to-slf4j")
    //     exclude("ch.qos.logback", "logback-classic")
    // }
    // implementation(libs.vaadin.spring.boot.starter) {
    //     exclude("org.apache.logging.log4j", "log4j-to-slf4j")
    //     exclude("ch.qos.logback", "logback-classic")
    // }

    // developmentOnly(libs.spring.boot.devtools)

    "standaloneImplementation"(sourceSets.main.map { it.output })
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
    register<JavaExec>("run") {
        description = "Runs the GUI with default (Dev) config"
        dependsOn(createRunDir)
        workingDir(createRunDir.map { it.outputs.files.singleFile })
        mainClass = "de.dasbabypixel.heating.gui.MainKt"
        classpath(standalone.map { it.output })
    }
}