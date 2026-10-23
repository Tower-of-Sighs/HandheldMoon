import net.darkhax.curseforgegradle.TaskPublishCurseForge
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources
import java.util.Properties

plugins {
    id("java-library")
    id("fabric-loom")
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
val trinkets_version: String by project
val tacz_id: String by project
val sodium_version: String by project

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
// (`gradlew -p common build`, see common/build.gradle.kts). Its sources are
// compiled into this target by source-set sharing; the exclusions below are the
// files whose 26.1 APIs have a confirmed 1.20.1 difference. This target ships
// its own implementation for each excluded path.
sourceSets {
    main {
        java.srcDir(layout.buildDirectory.dir("generated/common-main"))
    }
    test {
        java.srcDir("../../common/src/test/java")
        resources.srcDir("../../common/src/test/resources")
    }
}

tasks.register<Sync>("prepareCommonSources") {
    from("../../common/src/main/java") {
        exclude(
            "cc/sighs/handheldmoon/api/raycone/impl/RayConeRendererImpl.java",
            "cc/sighs/handheldmoon/api/raycone/RayConeGeometry.java",
            "cc/sighs/handheldmoon/block/FullMoonBlock.java",
            "cc/sighs/handheldmoon/block/FullMoonBlockEntity.java",
            "cc/sighs/handheldmoon/block/MoonlightLampBlock.java",
            "cc/sighs/handheldmoon/block/MoonlightLampBlockEntity.java",
            "cc/sighs/handheldmoon/client/LightSourceHighlightRenderer.java",
            "cc/sighs/handheldmoon/client/renderer/FullMoonRenderer.java",
            "cc/sighs/handheldmoon/client/renderer/MoonlightLampRenderer.java",
            "cc/sighs/handheldmoon/client/renderer/item/MoonlightLampPoweredProperty.java",
            "cc/sighs/handheldmoon/client/screen/AbstractFlashlightItemConfigScreen.java",
            "cc/sighs/handheldmoon/client/screen/AbstractLightSourceConfigScreen.java",
            "cc/sighs/handheldmoon/client/screen/FlashlightItemConfigScreen.java",
            "cc/sighs/handheldmoon/client/screen/HandheldMoonConfigScreen.java",
            "cc/sighs/handheldmoon/client/screen/LightSourceConfigScreen.java",
            "cc/sighs/handheldmoon/dynamiclight/DynamicLightEntityTint.java",
            "cc/sighs/handheldmoon/entity/FullMoonEntity.java",
            "cc/sighs/handheldmoon/event/handler/EffectManager.java",
            "cc/sighs/handheldmoon/event/handler/ShaderEventHandler.java",
            "cc/sighs/handheldmoon/item/ColorLensItem.java",
            "cc/sighs/handheldmoon/item/FullMoonItem.java",
            "cc/sighs/handheldmoon/item/MoonlightLampItem.java",
            "cc/sighs/handheldmoon/item/MoonlightWandItem.java",
            "cc/sighs/handheldmoon/mixin/client/EntityRendererMixin.java",
            "cc/sighs/handheldmoon/mixin/client/GameRendererAccessor.java",
            "cc/sighs/handheldmoon/mixin/client/PostChainAccessor.java",
            "cc/sighs/handheldmoon/mixin/client/PostPassAccessor.java",
            "cc/sighs/handheldmoon/mixin/dynamiclight/DynamicLightBlockModelLighterMixin.java",
            "cc/sighs/handheldmoon/mixin/dynamiclight/DynamicLightBrightnessGetterMixin.java",
            "cc/sighs/handheldmoon/mixin/dynamiclight/EntityModelTintMixin.java",
            "cc/sighs/handheldmoon/mixin/dynamiclight/LightmapColorMixin.java",
            "cc/sighs/handheldmoon/mixin/dynamiclight/SodiumDynamicLightBlockRendererMixin.java",
            "cc/sighs/handheldmoon/mixin/dynamiclight/SodiumSectionReadinessMixin.java",
            "cc/sighs/handheldmoon/network/ServerFullMoonConfigSyncPacket.java",
            "cc/sighs/handheldmoon/network/ServerHeldFullMoonConfigSyncPacket.java",
            "cc/sighs/handheldmoon/network/ServerHeldMoonlightLampConfigSyncPacket.java",
            "cc/sighs/handheldmoon/network/ServerMoonLightLampSyncPacket.java",
            "cc/sighs/handheldmoon/network/ServerMoonlightLampConfigSyncPacket.java",
            "cc/sighs/handheldmoon/network/ServerOpenFlashlightConfigPacket.java",
            "cc/sighs/handheldmoon/network/ServerOpenLampBlockConfigPacket.java",
            "cc/sighs/handheldmoon/network/ServerSetLightProfilePacket.java",
            "cc/sighs/handheldmoon/registry/ModDataComponent.java",
            "cc/sighs/handheldmoon/registry/ModKeyBindings.java",
            "cc/sighs/handheldmoon/registry/ModRegistries.java",
            "cc/sighs/handheldmoon/spi/AbstractClientPlatformService.java",
            "cc/sighs/handheldmoon/spi/RegistryService.java",
            "cc/sighs/handheldmoon/util/IrisCompat.java",
            "cc/sighs/handheldmoon/util/LineLightMath.java")
    }
    into(layout.buildDirectory.dir("generated/common-main"))
}

tasks.named("compileJava") { dependsOn("prepareCommonSources") }

tasks.named<Jar>("sourcesJar") {
    dependsOn("prepareCommonSources")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// 1.20.1 recipes, block models, item definitions, shaders and TACZ data use the
// legacy layout, so the 26.1 common resources are replaced by this target's own.
val prepareCommonResources = tasks.register<Sync>("prepareCommonResources") {
    from("../../common/src/main/resources") {
        exclude("data/handheldmoon/recipe/**")
        exclude("assets/handheldmoon/models/block/**")
        exclude("assets/handheldmoon/items/**")
        exclude("assets/handheldmoon/post_effect/**")
        exclude("assets/handheldmoon/shaders/post/**")
        exclude("META-INF/accesstransformer.cfg")
    }
    into(layout.buildDirectory.dir("common-resources"))
}

sourceSets.named("main") {
    resources.srcDir(prepareCommonResources)
    resources.srcDir("src/generated/resources")
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
    maven {
        name = "Jared"
        url = uri("https://maven.blamejared.com/")
        content { includeGroup("mezz.jei") }
    }
}

// Trinkets' API extends Cardinal Components' ComponentV3, but the mod ships CCA
// as a nested jar and Gradle does not put nested jars on the compile classpath.
// Resolve Trinkets separately and extract the bundled CCA base module for
// compilation only; Trinkets supplies the same classes at runtime.
val trinketsBundle: Configuration by configurations.creating

val extractCardinalComponentsBase = tasks.register<Copy>("extractCardinalComponentsBase") {
    from({ zipTree(trinketsBundle.singleFile) }) {
        include("META-INF/jars/cardinal-components-base-*.jar")
        eachFile { path = "cardinal-components-base.jar" }
        includeEmptyDirs = false
    }
    into(layout.buildDirectory.dir("dependencies"))
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft_version")
    add("mappings", loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:$fabric_loader_version")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabric_version")
    modCompileOnly("maven.modrinth:trinkets:$trinkets_version")
    trinketsBundle("maven.modrinth:trinkets:$trinkets_version")
    compileOnly(fileTree(layout.buildDirectory.dir("dependencies")) { include("*.jar") })
    modCompileOnly("curse.maven:tacz-refabricated-1334246:$tacz_id")
    // Compile-time API for the Sodium 0.5 chunk-render tint mixin, plus Sodium
    // itself in the dev run so the mixin is exercised (Loom remaps it).
    modCompileOnly("maven.modrinth:sodium:$sodium_version")
    modRuntimeOnly("maven.modrinth:sodium:$sodium_version")
    compileOnly("mezz.jei:jei-1.20.1-fabric:15.20.0.104")

    // Local jars in libs/ are development-only mods (AutoJS MCP endpoint + the
    // Rhino it embeds); Loom remaps them for the dev run and they are never
    // packaged into the mod jar. See AGENTS.md.
    file("libs/AutoJS-fabric-1.20.1-1.0.0.jar").takeIf { it.exists() }?.let {
        modRuntimeOnly(files(it))
    }
    file("libs/rhino-fabric-2001.2.2-build.17.jar").takeIf { it.exists() }?.let {
        modRuntimeOnly(files(it))
    }

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("it.unimi.dsi:fastutil:8.5.12")
}

tasks.named("compileJava") { dependsOn(extractCardinalComponentsBase) }
tasks.named("remapSourcesJar") { dependsOn(extractCardinalComponentsBase) }

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
    versionName.set(provider { "$mod_name ${project.version} (Fabric 1.20.1) [${releaseDist.get()}]" })
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
    description = "Publish Fabric 1.20.1 artifact to CurseForge."
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
    description = "Publish Fabric 1.20.1 artifact to Modrinth and CurseForge."
    dependsOn("modrinth", "publishCurseForge")
}
