import net.darkhax.curseforgegradle.TaskPublishCurseForge
import org.gradle.api.attributes.Attribute
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources

plugins {
    id("java-library")
    id("net.fabricmc.fabric-loom")
    id("com.modrinth.minotaur")
    id("net.darkhax.curseforgegradle")
}

evaluationDependsOn(":common")
val commonProject = project(":common")
val commonMainSourceSet = commonProject.extensions.getByType<SourceSetContainer>().named("main").get()
commonProject.extensions.configure<JavaPluginExtension> {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}
commonProject.dependencies.add("compileOnly", files(configurations.getByName("compileClasspath").incoming.artifactView {
    componentFilter { component -> component is ModuleComponentIdentifier }
}.files))

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    withSourcesJar()
    withJavadocJar()
}

sourceSets.named("main") {
    resources.srcDir("../../common/src/main/resources")
}

tasks.named<ProcessResources>("processResources") {
    val expandProps = mapOf(
        "version" to project.version,
        "minecraft_version" to minecraft_version,
        "mod_name" to mod_name,
        "mod_id" to mod_id,
        "fabric_loader_version" to fabric_loader_version,
        "fabric_version" to fabric_version,
        "java_version" to project.property("java_version"),
        "license" to project.property("license"),
        "mod_author" to project.property("mod_author"),
        "description" to project.description.orEmpty(),
    )
    val jsonExpandProps = expandProps.mapValues { (_, value) ->
        if (value is String) value.replace("\n", "\\\\n") else value
    }
    filesMatching(listOf("fabric.mod.json", "*.mixins.json", "pack.mcmeta")) {
        expand(jsonExpandProps)
    }
    inputs.properties(expandProps)
}

val minecraft_version: String by project
val fabric_loader_version: String by project
val fabric_version: String by project
val mod_id: String by project
val mod_name: String by project

repositories {
    maven {
        name = "Curse Maven"
        url = uri("https://www.cursemaven.com")
        content { includeGroup("curse.maven") }
    }
    maven { name = "Modrinth"; url = uri("https://api.modrinth.com/maven") }
}

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
// Optional POM dependency whitelist for Maven publication.
// Example:
// extra["mavenDependencyWhitelist"] = listOf(
//     "group.id",
//     "artifact-id",
//     "group.id:artifact-id",
// )
extra["mavenDependencyWhitelist"] = emptySet<String>()

repositories {
    maven {
        name = "Terraformers"
        url = uri("https://maven.terraformersmc.com/releases/")
    }
}
dependencies {
    implementation(commonProject)
    minecraft("com.mojang:minecraft:$minecraft_version")
    implementation("net.fabricmc:fabric-loader:$fabric_loader_version")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabric_version")
    implementation("com.terraformersmc:modmenu:18.0.0-alpha.8")
    implementation("curse.maven:irisshaders-455508:7867943")
    implementation("curse.maven:sodium-394468:7867826")
    implementation("maven.modrinth:trinkets-updated:ObfwZi0X")
}

tasks.named("compileJava") {
    dependsOn(commonProject.tasks.named("classes"))
}

tasks.named<Jar>("jar") {
    dependsOn(commonProject.tasks.named("classes"))
    from(commonProject.extensions.getByType<org.gradle.api.tasks.SourceSetContainer>().named("main").map { it.output.classesDirs })
}

tasks.named<Jar>("sourcesJar") {
    from(commonProject.extensions.getByType<org.gradle.api.tasks.SourceSetContainer>().named("main").map { it.java })
}

loom {
    val aw = file("../../common/src/main/resources/${mod_id}.classtweaker")
    if (aw.exists()) {
        accessWidenerPath.set(aw)
    }
    mods {
        create(mod_id) {
            sourceSet(sourceSets.main.get())
            sourceSet(commonMainSourceSet)
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

// Implement mcgradleconventions loader attribute
val loaderAttribute = Attribute.of("io.github.mcgradleconventions.loader", String::class.java)
listOf("apiElements", "runtimeElements", "sourcesElements", "javadocElements", "includeInternal", "modCompileClasspath").forEach { variant ->
    configurations.named(variant) {
        attributes {
            attribute(loaderAttribute, "fabric")
        }
    }
}
sourceSets.configureEach {
    listOf(compileClasspathConfigurationName, runtimeClasspathConfigurationName).forEach { variant ->
        configurations.named(variant) {
            attributes {
                attribute(loaderAttribute, "fabric")
            }
        }
    }
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
        !curseforgeToken.orNull.isNullOrBlank() &&
            !curseforgeProjectId.orNull.isNullOrBlank()
    }
}

tasks.register("publishToPlatformServices") {
    group = "publishing"
    description = "Publish Fabric artifact to Modrinth and CurseForge."
    dependsOn("modrinth", "publishCurseForge")
}
