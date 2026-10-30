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
    implementation(libs.bundles.logging) // implementation(libs.bundles.spring) {
    //     exclude("org.apache.logging.log4j", "log4j-to-slf4j")
    //     exclude("ch.qos.logback", "logback-classic")
    // }
    // implementation(libs.vaadin.spring.boot.starter) {
    //     exclude("org.apache.logging.log4j", "log4j-to-slf4j")
    //     exclude("ch.qos.logback", "logback-classic")
    // }

    // developmentOnly(libs.spring.boot.devtools)

    implementation("org.eclipse.jetty:jetty-server:12.1.14")
    implementation("org.eclipse.jetty.ee10:jetty-ee10-servlet:12.1.14")
    implementation("com.github.spullara.mustache.java:compiler:0.9.14")
    implementation("com.auth0:java-jwt:4.6.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.22.3")

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

abstract class GenerateResourceIndexTask : DefaultTask() {
    @get:InputDirectory
    abstract val resourceRoot: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val targetPackage: Property<String>

    @TaskAction
    fun generate() {
        val root = resourceRoot.get().asFile
        val out = outputDir.get().asFile
        out.listFiles().forEach {
            it.deleteRecursively()
        }

        val todo = ArrayList<File>()
        todo.addLast(root)
        while (todo.isNotEmpty()) {
            val file = todo.removeLast()
            val index = file.list()
            val relative = file.relativeTo(root).toString()
            println("$relative: ${index.contentToString()}")
            out.resolve(relative).also { it.mkdirs() }.resolve(".index.dat").also { it.createNewFile() }
                .bufferedWriter().use { writer ->
                    index.forEach {
                        writer.write(it)
                        writer.write("\n")
                    }
                }
            index.forEach { name ->
                val otherFile = file.resolve(name)
                if (otherFile.isDirectory) {
                    todo.add(otherFile)
                }
            }

        }
    }
}

val generateResourceIndex = tasks.register<GenerateResourceIndexTask>("generateResourceIndex") {
    resourceRoot.set(layout.projectDirectory.dir("src/main/resources"))
    targetPackage.set("com.example.mypackage")
    outputDir.set(layout.buildDirectory.dir("generated/resources/resourceIndex"))
}

sourceSets.main {
    resources.srcDir(generateResourceIndex.map { it.outputDir })
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }
    shadowJar {
        manifest {
            attributes("Main-Class" to "de.dasbabypixel.heating.gui.MainKt")
        }
    }
    register<JavaExec>("run") {
        description = "Runs the GUI with default (Dev) config"
        dependsOn(createRunDir)
        workingDir(createRunDir.map { it.outputs.files.singleFile })
        mainClass = "de.dasbabypixel.heating.gui.MainKt"
        classpath(standalone.map { it.output })
    }
}