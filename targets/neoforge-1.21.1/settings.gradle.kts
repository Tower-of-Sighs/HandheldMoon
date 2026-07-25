pluginManagement {
    includeBuild("../../buildSrc") {
        name = "handheldmoon-build-conventions"
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
        exclusiveContent {
            forRepository {
                maven {
                    name = "Fabric"
                    url = uri("https://maven.fabricmc.net")
                }
            }
            filter {
                includeGroupAndSubgroups("net.fabricmc")
            }
        }
        exclusiveContent {
            forRepository {
                maven {
                    name = "Sponge"
                    url = uri("https://repo.spongepowered.org/repository/maven-public")
                }
            }
            filter {
                includeGroupAndSubgroups("org.spongepowered")
            }
        }
    }
    plugins {
        id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT"
        id("net.neoforged.moddev") version "2.0.141"
        id("com.modrinth.minotaur") version "2.9.0"
        id("net.darkhax.curseforgegradle") version "1.1.24"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "${providers.gradleProperty("mod_name").get()}-${rootDir.name}"
include("common")
project(":common").projectDir = file("../../common")
