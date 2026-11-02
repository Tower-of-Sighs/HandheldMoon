pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = providers.gradleProperty("mod_name").get()

// common/ is a shared source tree with its own settings file. It is attached to
// each target as a composite build (includeBuild) and compiled into that target
// through source-set sharing, so it is not included here.
