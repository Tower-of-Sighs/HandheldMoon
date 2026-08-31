import net.darkhax.curseforgegradle.TaskPublishCurseForge
import org.gradle.api.attributes.Attribute
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources

plugins {
    id("java-library")
    id("net.neoforged.moddev")
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

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

sourceSets.named("main") {
    resources.srcDir("../../common/src/main/resources")
}

tasks.named<ProcessResources>("processResources") {
    val expandProps = mapOf(
        "version" to project.version,
        "minecraft_version" to minecraft_version,
        "minecraft_version_range" to project.property("minecraft_version_range"),
        "mod_name" to mod_name,
        "mod_id" to mod_id,
        "neoforge_version" to neoforge_version,
        "neoforge_loader_version_range" to project.property("neoforge_loader_version_range"),
        "license" to project.property("license"),
        "mod_author" to project.property("mod_author"),
        "credits" to project.property("credits"),
        "description" to project.description.orEmpty(),
    )
    val jsonExpandProps = expandProps.mapValues { (_, value) ->
        if (value is String) value.replace("\n", "\\\\n") else value
    }
    filesMatching(listOf("META-INF/neoforge.mods.toml", "*.mixins.json", "pack.mcmeta")) {
        expand(jsonExpandProps)
    }
    inputs.properties(expandProps)
}

val neoforge_version: String by project
val mod_id: String by project
val minecraft_version: String by project
val mod_name: String by project

repositories {
    maven {
        name = "Shedaniel"
        url = uri("https://maven.shedaniel.me")
    }
    maven {
        name = "Curse Maven"
        url = uri("https://www.cursemaven.com")
        content { includeGroup("curse.maven") }
    }
    maven { name = "Modrinth"; url = uri("https://api.modrinth.com/maven") }
    maven { name = "Curios Api"; url = uri("https://maven.theillusivec4.top/") }
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
    val loaderSpecific = providers.gradleProperty("modrinth_project_neoforge").orNull?.trim().orEmpty()
    if (loaderSpecific.isNotEmpty()) loaderSpecific else providers.gradleProperty("modrinth_project").orNull?.trim().orEmpty()
}
val curseforgeProjectId = providers.provider {
    val loaderSpecific = providers.gradleProperty("curseforge_project_neoforge").orNull?.trim().orEmpty()
    if (loaderSpecific.isNotEmpty()) loaderSpecific else providers.gradleProperty("curseforge_project").orNull?.trim().orEmpty()
}
val curios_version: String by project

// Optional POM dependency whitelist for Maven publication.
// Example:
// extra["mavenDependencyWhitelist"] = listOf(
//     "group.id",
//     "artifact-id",
//     "group.id:artifact-id",
// )
extra["mavenDependencyWhitelist"] = emptySet<String>()

dependencies {
    implementation(commonProject)
    implementation ("top.theillusivec4.curios:curios-neoforge:$curios_version")
    implementation("curse.maven:irisshaders-455508:7867946")
    implementation("curse.maven:sodium-394468:7867828")
}

neoForge {
    version = neoforge_version

    // Automatically enable neoforge AccessTransformers if the file exists
    val at = file("../../common/src/main/resources/META-INF/accesstransformer.cfg")
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
            clientData()
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
            sourceSet(commonMainSourceSet)
        }
    }
}

tasks.named("compileJava") {
    dependsOn(commonProject.tasks.named("classes"))
}

val commonMinecraftCompileClasspath = configurations.create("commonMinecraftCompileClasspath") {
    isCanBeConsumed = false
    isCanBeResolved = true
    extendsFrom(configurations.getByName("modDevCompileDependencies"))
}
commonProject.dependencies.add("compileOnly", files(commonMinecraftCompileClasspath))

tasks.named<Jar>("jar") {
    dependsOn(commonProject.tasks.named("classes"))
    from(commonProject.extensions.getByType<org.gradle.api.tasks.SourceSetContainer>().named("main").map { it.output.classesDirs })
}

tasks.named<Jar>("sourcesJar") {
    from(commonProject.extensions.getByType<org.gradle.api.tasks.SourceSetContainer>().named("main").map { it.java })
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
