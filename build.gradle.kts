import kotlin.io.path.createDirectories
import kotlin.io.path.notExists

plugins {
    alias(libs.plugins.spring)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.shadow) //    alias(libs.plugins.vaadin)
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
    implementation(libs.gson)
    implementation(libs.hikaricp)
    implementation(libs.mysql.connector.j)
    implementation(libs.bundles.spring) {
        exclude("org.apache.logging.log4j", "log4j-to-slf4j")
        exclude("ch.qos.logback", "logback-classic")
    }
    implementation(libs.spring.boot.starter.webflux) {
        exclude("org.apache.logging.log4j", "log4j-to-slf4j")
        exclude("ch.qos.logback", "logback-classic")
    }
    testImplementation(kotlin("test"))
    testImplementation("org.mockito:mockito-core:5.11.0")

    developmentOnly(libs.spring.boot.devtools)
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
    jvmArgs("-Dserver.port=4242")
}

application {
    mainClass = "de.dasbabypixel.heating.MainKt"
}
