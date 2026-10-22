val targetBuilds = mapOf(
    "fabric-26.1" to "buildFabric261",
    "neoforge-1.21.1" to "buildNeoForge1211",
    "neoforge-26.1" to "buildNeoForge261",
)

fun Exec.buildInTarget(target: String) {
    group = "build"
    description = "Build $target in its independent Gradle project."
    workingDir(rootDir.resolve("targets/$target"))
    if (System.getProperty("os.name").lowercase().contains("windows")) {
        commandLine("cmd", "/c", "gradlew.bat", "build", "--console", "plain", "--no-daemon")
    } else {
        commandLine("./gradlew", "build", "--console", "plain", "--no-daemon")
    }
}

targetBuilds.forEach { (target, taskName) ->
    tasks.register<Exec>(taskName) {
        buildInTarget(target)
    }
}

// common/ is a shared source tree that also compiles standalone against the
// core target's NeoForge classpath.
tasks.register<Exec>("buildCommon") {
    group = "build"
    description = "Build the shared common source tree only."
    workingDir(rootDir)
    if (System.getProperty("os.name").lowercase().contains("windows")) {
        commandLine("cmd", "/c", "gradlew.bat", "-p", "common", "build", "--console", "plain", "--no-daemon")
    } else {
        commandLine("./gradlew", "-p", "common", "build", "--console", "plain", "--no-daemon")
    }
}

tasks.register("check") {
    group = "verification"
    description = "Compiles the shared common source tree."
    dependsOn("buildCommon")
}

tasks.register<Exec>("syncProjectFromProperties") {
    group = "automation"
    description = "Sync package/main class/mod id/license from gradle.properties."
    commandLine("python", "sync_project.py", "sync")
}

tasks.register<Exec>("generateClassTweakerFromAt") {
    group = "automation"
    description = "Generate each 26.1 target's handheldmoon.classtweaker from accesstransformer.cfg."
    commandLine("python", "sync_project.py", "generate-classtweaker")
}
