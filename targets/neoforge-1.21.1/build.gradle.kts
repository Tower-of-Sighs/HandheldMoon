import net.darkhax.curseforgegradle.TaskPublishCurseForge
import org.gradle.api.attributes.Attribute
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType

plugins {
    id("multiloader-loader")
    id("net.neoforged.moddev")
    id("com.modrinth.minotaur")
    id("net.darkhax.curseforgegradle")
}

val neoforge_version: String by project
val mod_id: String by project
val minecraft_version: String by project
val mod_name: String by project

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
    val loaderSpecific = providers.gradleProperty("modrinth_project_neoforge").orNull?.trim().orEmpty()
    if (loaderSpecific.isNotEmpty()) loaderSpecific else providers.gradleProperty("modrinth_project").orNull?.trim().orEmpty()
}
val curseforgeProjectId = providers.provider {
    val loaderSpecific = providers.gradleProperty("curseforge_project_neoforge").orNull?.trim().orEmpty()
    if (loaderSpecific.isNotEmpty()) loaderSpecific else providers.gradleProperty("curseforge_project").orNull?.trim().orEmpty()
}
val curios_version: String by project
val cloth_config_version: String by project
val jei_version: String by project
val iris_version: String by project
val sodium_version: String by project
val sable_version: String by project
val sable_companion_version: String by project

// Optional POM dependency whitelist for Maven publication.
// Example:
// extra["mavenDependencyWhitelist"] = listOf(
//     "group.id",
//     "artifact-id",
//     "group.id:artifact-id",
// )
extra["mavenDependencyWhitelist"] = emptyList<String>()

repositories {
    maven {
        name = "Shedaniel"
        url = uri("https://maven.shedaniel.me")
    }
    maven {
        name = "RyanHCode"
        url = uri("https://maven.ryanhcode.dev/releases")
    }
}

evaluationDependsOn(":common")
val commonMainSourceSet = project(":common").extensions.getByType<SourceSetContainer>().named("main")

dependencies {
    implementation("curse.maven:carry-on-274259:7393892")
    implementation("maven.modrinth:jei:$jei_version")
    implementation("maven.modrinth:curios:$curios_version")

    implementation("maven.modrinth:sodium:$sodium_version")
    implementation("curse.maven:sodium-extra-447673:5913377")
    implementation("maven.modrinth:iris:$iris_version")

    implementation("me.shedaniel.cloth:cloth-config-neoforge:$cloth_config_version")
    implementation("curse.maven:tacz-1-21-1-1353462:7295633")

    implementation("curse.maven:mafglib-910766:6895587")
    implementation("curse.maven:tweakerge-915857:7123684")

    implementation("maven.modrinth:sable:$sable_version")
    compileOnly("dev.ryanhcode.sable-companion:sable-companion-common-1.21.1:$sable_companion_version") {
        attributes {
            attribute(Attribute.of("io.github.mcgradleconventions.loader", String::class.java), "common")
        }
    }

    implementation("maven.modrinth:create-aeronautics:1.1.3+mc1.21.1")
    implementation("curse.maven:create-328085:7963363")
    implementation("curse.maven:architectury-api-419699:5786327")

    compileOnly("io.github.llamalad7:mixinextras-common:0.3.5")
}

neoForge {
    version = neoforge_version

    // Automatically enable neoforge AccessTransformers if the file exists
    val at = file("src/main/resources/META-INF/accesstransformer.cfg")
    if (at.exists()) {
        accessTransformers.from(at.absolutePath)
    }

    runs {
        configureEach {
            systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
            ideName = "NeoForge ${name.replaceFirstChar { it.uppercase() }} (${project.path})"
        }
        create("client") {
            client()
            gameDirectory.set(project.file("runs/client"))
        }
        create("data") {
            data()
            gameDirectory.set(project.file("runs/data"))
            // DataGen can be run by - "./gradlew :neoforge:runData" in Terminal.
            // Specify the modid for data generation, where to output the resulting resource, and where to look for existing resources.
            programArguments.addAll(
                "--mod",
                mod_id,
                "--all",
                "--output",
                project.file("src/generated/resources/").absolutePath,
                "--existing",
                project.file("src/main/resources/").absolutePath,
            )
        }
        create("server") {
            server()
            gameDirectory.set(project.file("runs/server"))
        }
    }

    mods {
        create(mod_id) {
            sourceSet(sourceSets.main.get())
            sourceSet(commonMainSourceSet.get())
        }
    }
}

sourceSets.named("main") {
    resources.srcDir("src/generated/resources")
}

// Implement mcgradleconventions loader attribute
val loaderAttribute = Attribute.of("io.github.mcgradleconventions.loader", String::class.java)
listOf("apiElements", "runtimeElements", "sourcesElements", "javadocElements").forEach { variant ->
    configurations.named(variant) {
        attributes {
            attribute(loaderAttribute, "neoforge")
        }
    }
}
sourceSets.configureEach {
    listOf(compileClasspathConfigurationName, runtimeClasspathConfigurationName, getTaskName(null, "jarJar")).forEach { variant ->
        configurations.named(variant) {
            attributes {
                attribute(loaderAttribute, "neoforge")
            }
        }
    }
}

modrinth {
    token.set(modrinthToken)
    projectId.set(modrinthProjectId)
    versionNumber.set(provider { "${project.version}+neoforge" })
    versionName.set(provider { "$mod_name ${project.version} (NeoForge) [${releaseDist.get()}]" })
    versionType.set(releaseType)
    changelog.set(publishChangelog)
    uploadFile.set(tasks.named("jar"))
    gameVersions.add(minecraft_version)
    loaders.add("neoforge")
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
    description = "Publish NeoForge artifact to CurseForge."
    apiToken = curseforgeToken.orNull

    val resolvedProjectId = curseforgeProjectId.orNull
    if (!resolvedProjectId.isNullOrBlank()) {
        val mainFile = upload(resolvedProjectId, tasks.named("jar"))
        mainFile.releaseType = releaseType.get()
        mainFile.changelog = publishChangelog.get()
        mainFile.changelogType = "markdown"
        mainFile.addGameVersion(minecraft_version)
        mainFile.addModLoader("NeoForge")
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
    description = "Publish NeoForge artifact to Modrinth and CurseForge."
    dependsOn("modrinth", "publishCurseForge")
}
