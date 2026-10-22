import net.darkhax.curseforgegradle.TaskPublishCurseForge
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources
import java.util.Properties

plugins {
    id("java-library")
    id("net.fabricmc.fabric-loom")
    id("com.modrinth.minotaur")
    id("net.darkhax.curseforgegradle")
}

// Shared mod metadata lives in the repository root; this target's own file
// supplies loader/version-specific values and overrides.
val sharedProperties = Properties()
file("../../gradle.properties").inputStream().use { sharedProperties.load(it) }
file("gradle.properties").inputStream().use { sharedProperties.load(it) }
sharedProperties.forEach { key, value -> extra.set(key.toString(), value.toString()) }

group = sharedProperties.getProperty("group")
version = sharedProperties.getProperty("version")

val java_version: String by project
val minecraft_version: String by project
val fabric_version: String by project
val fabric_loader_version: String by project
val mod_id: String by project
val mod_name: String by project
val license: String by project
val mod_author: String by project
val iris_id: String by project
val sodium_id: String by project
val trinkets_version: String by project

base {
    archivesName.set("$mod_name-fabric-$minecraft_version")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(java_version.toInt())
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = java_version.toInt()
}

// common/ is a shared source tree that also compiles standalone
// (`gradlew -p common build`, see common/build.gradle.kts). Everything without a
// confirmed version difference lives there, so its sources are compiled into
// this target by source-set sharing.
sourceSets {
    main {
        java.srcDir(layout.buildDirectory.dir("generated/common-main"))
        resources.srcDir("../../common/src/main/resources")
    }
    test {
        java.srcDir("../../common/src/test/java")
        resources.srcDir("../../common/src/test/resources")
    }
}

tasks.register<Sync>("prepareCommonSources") {
    from("../../common/src/main/java") {
        // This target's version differences are registered here: exclude one
        // path per common file that this target replaces with its own
        // implementation.
    }
    into(layout.buildDirectory.dir("generated/common-main"))
}

tasks.named("compileJava") { dependsOn("prepareCommonSources") }

tasks.named<Jar>("sourcesJar") {
    dependsOn("prepareCommonSources")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.named<ProcessResources>("processResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    val expandProps = mapOf(
        "version" to project.version,
        "minecraft_version" to minecraft_version,
        "mod_name" to mod_name,
        "mod_id" to mod_id,
        "fabric_loader_version" to fabric_loader_version,
        "fabric_version" to fabric_version,
        "java_version" to java_version,
        "license" to license,
        "mod_author" to mod_author,
        "description" to (sharedProperties.getProperty("description") ?: ""),
    )
    val jsonExpandProps = expandProps.mapValues { (_, value) ->
        if (value is String) value.replace("\n", "\\\\n") else value
    }
    filesMatching(listOf("fabric.mod.json", "*.mixins.json", "pack.mcmeta")) {
        expand(jsonExpandProps)
    }
    inputs.properties(expandProps)
}

repositories {
    mavenCentral()
    maven {
        name = "Curse Maven"
        url = uri("https://www.cursemaven.com")
        content { includeGroup("curse.maven") }
    }
    maven { name = "Modrinth"; url = uri("https://api.modrinth.com/maven") }
    maven {
        name = "Terraformers"
        url = uri("https://maven.terraformersmc.com/releases/")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft_version")
    implementation("net.fabricmc:fabric-loader:$fabric_loader_version")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabric_version")
    implementation("com.terraformersmc:modmenu:18.0.0-alpha.8")
    implementation("curse.maven:irisshaders-455508:$iris_id")
    implementation("curse.maven:sodium-394468:$sodium_id")
    implementation("maven.modrinth:trinkets-updated:$trinkets_version")

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("it.unimi.dsi:fastutil:8.5.12")
}

loom {
    val aw = file("src/main/resources/${mod_id}.classtweaker")
    if (aw.exists()) {
        accessWidenerPath.set(aw)
    }
    mods {
        create(mod_id) {
            sourceSet(sourceSets.main.get())
        }
    }
    runs {
        named("client") {
            client()
            configName = "Fabric Client"
            ideConfigGenerated(true)
            runDir("runs/client")
        }
        named("server") {
            server()
            configName = "Fabric Server"
            ideConfigGenerated(true)
            runDir("runs/server")
        }
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

// Release publishing ---------------------------------------------------------

val releaseType = providers.gradleProperty("release_type").orElse("release")
val releaseDist = providers.gradleProperty("release_dist").orElse("both").map { value ->
    when (value.trim().lowercase()) {
        "client", "server", "both" -> value.trim().lowercase()
        else -> "both"
    }
}
val releaseChangelog = providers.gradleProperty("release_changelog").orElse("No changelog was specified.")
val publishChangelog = providers.provider {
    "[dist=${releaseDist.get()}]\n\n${releaseChangelog.get()}"
}
val modrinthToken = providers.gradleProperty("modrinth_token").orElse(providers.environmentVariable("MODRINTH_TOKEN"))
val curseforgeToken = providers.gradleProperty("curseforge_token").orElse(providers.environmentVariable("CURSEFORGE_TOKEN"))
val modrinthProjectId = providers.provider {
    val loaderSpecific = providers.gradleProperty("modrinth_project_fabric").orNull?.trim().orEmpty()
    if (loaderSpecific.isNotEmpty()) loaderSpecific else providers.gradleProperty("modrinth_project").orNull?.trim().orEmpty()
}
val curseforgeProjectId = providers.provider {
    val loaderSpecific = providers.gradleProperty("curseforge_project_fabric").orNull?.trim().orEmpty()
    if (loaderSpecific.isNotEmpty()) loaderSpecific else providers.gradleProperty("curseforge_project").orNull?.trim().orEmpty()
}
val publishArtifact = provider {
    tasks.findByName("remapJar") ?: tasks.named("jar").get()
}

modrinth {
    token.set(modrinthToken)
    projectId.set(modrinthProjectId)
    versionNumber.set(provider { "${project.version}+fabric" })
    versionName.set(provider { "$mod_name ${project.version} (Fabric) [${releaseDist.get()}]" })
    versionType.set(releaseType)
    changelog.set(publishChangelog)
    uploadFile.set(publishArtifact)
    gameVersions.add(minecraft_version)
    loaders.add("fabric")
    detectLoaders.set(false)
}

tasks.named("modrinth") {
    onlyIf {
        !modrinthToken.orNull.isNullOrBlank() &&
            !modrinthProjectId.orNull.isNullOrBlank()
    }
}

tasks.register<TaskPublishCurseForge>("publishCurseForge") {
    group = "publishing"
    description = "Publish Fabric artifact to CurseForge."
    apiToken = curseforgeToken.orNull

    val resolvedProjectId = curseforgeProjectId.orNull
    if (!resolvedProjectId.isNullOrBlank()) {
        val mainFile = upload(resolvedProjectId, publishArtifact.get())
        mainFile.releaseType = releaseType.get()
        mainFile.changelog = publishChangelog.get()
        mainFile.changelogType = "markdown"
        mainFile.addGameVersion(minecraft_version)
        mainFile.addModLoader("Fabric")
        when (releaseDist.get()) {
            "client" -> mainFile.addEnvironment("Client")
            "server" -> mainFile.addEnvironment("Server")
            else -> mainFile.addEnvironment("Client", "Server")
        }
    }

    onlyIf {
        !curseforgeToken.orNull.isNullOrBlank() && !curseforgeProjectId.orNull.isNullOrBlank()
    }
}

tasks.register("publishToPlatformServices") {
    group = "publishing"
    description = "Publish Fabric artifact to Modrinth and CurseForge."
    dependsOn("modrinth", "publishCurseForge")
}
