pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven {
            name = "MinecraftForge"
            url = uri("https://maven.minecraftforge.net/")
        }
    }
    plugins {
        id("net.neoforged.moddev.legacyforge") version "2.0.91"
        id("com.modrinth.minotaur") version "2.9.0"
        id("net.darkhax.curseforgegradle") version "1.1.24"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "HandheldMoon-forge-1.20.1"

// Attach the shared source tree as a composite build so IDEs (IntelliJ) import
// common/ as a separate, independently-editable module with its own classpath.
// Targets still compile common sources via source-set sharing; this include does
// not add a project dependency.
includeBuild("../../common")
