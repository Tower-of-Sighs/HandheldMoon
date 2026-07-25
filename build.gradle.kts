plugins {
    // see https://fabricmc.net/develop/ for new versions
    id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT" apply false
    // see https://projects.neoforged.net/neoforged/moddevgradle for new versions
    id("net.neoforged.moddev") version "2.0.141" apply false
    id("com.modrinth.minotaur") version "2.9.0" apply false
    id("net.darkhax.curseforgegradle") version "1.1.24" apply false
}

val targetBuilds = mapOf(
    "fabric-26.1" to "buildFabric261",
    "neoforge-1.21.1" to "buildNeoForge1211",
    "neoforge-26.1" to "buildNeoForge261",
)

targetBuilds.forEach { (target, taskName) ->
    tasks.register<Exec>(taskName) {
        group = "build"
        description = "Build $target in its independent Gradle project."
        workingDir(rootDir.resolve("targets/$target"))
        if (System.getProperty("os.name").lowercase().contains("windows")) {
            commandLine("cmd", "/c", "gradlew.bat", "build", "--console", "plain", "--no-daemon")
        } else {
            commandLine("./gradlew", "build", "--console", "plain", "--no-daemon")
        }
    }
}

tasks.register("buildCommon") {
    group = "build"
    description = "Build the shared common project only."
    dependsOn(":common:build")
}

tasks.register<Exec>("syncProjectFromProperties") {
    group = "automation"
    description = "Sync package/main class/mod id/license from gradle.properties."
    commandLine("python", "sync_project.py", "sync")
}

tasks.register<Exec>("generateClassTweakerFromAt") {
    group = "automation"
    description = "Generate common/src/main/resources/<mod_id>.classtweaker from accesstransformer.cfg."
    commandLine("python", "sync_project.py", "generate-classtweaker")
}

