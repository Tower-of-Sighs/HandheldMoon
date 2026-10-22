// Standalone build for the shared source tree.
//
// common/ holds every piece of code that has no confirmed loader/version
// difference. It MAY reference Minecraft and loader types: the split rule is
// "a confirmed version difference", not "platform independence". It compiles
// against the same NeoForge 26.1 classpath as targets/neoforge-26.1 (the core
// target), so it can be built and tested on its own:
//
//     gradlew -p common build
//
// Targets do NOT depend on this as a Gradle project. They compile these same
// sources into their own source set through `prepareCommonSources` (see each
// target's build.gradle.kts), and that task is also where a target excludes the
// common files that have a version difference.
import java.util.Properties

plugins {
    `java-library`
    id("net.neoforged.moddev") version "2.0.141"
}

val sharedProperties = Properties()
file("../gradle.properties").inputStream().use { sharedProperties.load(it) }
file("gradle.properties").inputStream().use { sharedProperties.load(it) }
sharedProperties.forEach { key, value -> extra.set(key.toString(), value.toString()) }

group = sharedProperties.getProperty("group")
version = sharedProperties.getProperty("version")

val java_version: String by project
val neoforge_version: String by project
val iris_id: String by project
val sodium_id: String by project

java {
    toolchain.languageVersion = JavaLanguageVersion.of(java_version.toInt())
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

repositories {
    mavenCentral()
    maven {
        name = "Curse Maven"
        url = uri("https://www.cursemaven.com")
        content { includeGroup("curse.maven") }
    }
    maven {
        name = "Modrinth"
        url = uri("https://api.modrinth.com/maven")
    }
}

// Sodium's block-render API that the dynamic-light mixins target lives in the
// jarjar payload of the NeoForge artifact, not in its outer bootstrap jar.
val sodiumNestedCompile = configurations.create("sodiumNestedCompile") {
    isCanBeConsumed = false
    isCanBeResolved = true
}
val sodiumNestedPath = "META-INF/jarjar/net.caffeinemc.sodium-neoforge-0.8.9+mc26.1.1-mod.jar"
val extractSodiumCompileJar = tasks.register<Copy>("extractSodiumCompileJar") {
    from({ sodiumNestedCompile.files.map { zipTree(it) } }) {
        include(sodiumNestedPath)
    }
    into(layout.buildDirectory.dir("sodium-compile"))
}
val sodiumCompileJar = files(layout.buildDirectory.file("sodium-compile/$sodiumNestedPath"))
    .builtBy(extractSodiumCompileJar)

dependencies {
    // Optional mod APIs that shared compat code and mixins compile against.
    compileOnly("curse.maven:irisshaders-455508:$iris_id")
    compileOnly("curse.maven:sodium-394468:$sodium_id")
    sodiumNestedCompile("curse.maven:sodium-394468:$sodium_id")
    compileOnly(sodiumCompileJar)

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("it.unimi.dsi:fastutil:8.5.12")
}

neoForge {
    version = neoforge_version

    val at = file("accesstransformer.cfg")
    if (at.exists()) {
        accessTransformers.from(at.absolutePath)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
