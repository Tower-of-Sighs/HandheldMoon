pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven {
            name = "NeoForged"
            url = uri("https://maven.neoforged.net/releases")
        }
    }
    plugins {
        id("net.neoforged.moddev") version "2.0.141"
        id("com.modrinth.minotaur") version "2.9.0"
        id("net.darkhax.curseforgegradle") version "1.1.24"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "HandheldMoon-neoforge-26.1"

// Attach the shared source tree as a composite build so IDEs (IntelliJ) import
// common/ as a separate, independently-editable module with its own classpath.
// Targets still compile common sources via source-set sharing; this include does
// not add a project dependency.
includeBuild("../../common")
