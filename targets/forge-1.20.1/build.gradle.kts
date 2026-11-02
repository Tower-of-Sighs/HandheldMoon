import net.darkhax.curseforgegradle.TaskPublishCurseForge
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources
import java.util.Properties

plugins {
    `java-library`
    id("net.neoforged.moddev.legacyforge")
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
val minecraft_version_range: String by project
val forge_version: String by project
val forge_version_range: String by project
val loader_version_range: String by project
val mod_id: String by project
val mod_name: String by project
val license: String by project
val mod_author: String by project
val curios_version: String by project
val jei_version: String by project
val tacz_version: String by project
val embeddium_version: String by project

base {
    archivesName.set("$mod_name-forge-$minecraft_version")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(java_version.toInt())
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
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
            "cc/sighs/handheldmoon/util/LineLightMath.java",
        )
    }
    into(layout.buildDirectory.dir("generated/common-main"))
}

tasks.named("compileJava") { dependsOn("prepareCommonSources") }

tasks.named<Jar>("sourcesJar") {
    dependsOn("prepareCommonSources")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// 1.20.1 recipes, block models, item definitions and shaders use the legacy
// layout, so the 26.1 common resources are replaced by this target's own.
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
        "minecraft_version_range" to minecraft_version_range,
        "mod_name" to mod_name,
        "mod_id" to mod_id,
        "forge_version" to forge_version,
        "forge_version_range" to forge_version_range,
        "loader_version_range" to loader_version_range,
        "license" to license,
        "mod_author" to mod_author,
        "description" to (sharedProperties.getProperty("description") ?: ""),
    )
    inputs.properties(expandProps)
    filesMatching(listOf("META-INF/mods.toml", "*.mixins.json", "pack.mcmeta")) {
        expand(expandProps)
    }
}

repositories {
    mavenCentral()
    // Optional local mirror of already-downloaded mod jars (offline/slow-network
    // builds); it uses the same coordinates as the remote repositories and is
    // gitignored, so CI resolves everything from Modrinth / Curse Maven instead.
    if (file("libs-maven").isDirectory) {
        maven {
            name = "LocalLibs"
            url = uri("libs-maven")
        }
    }
    // Forge's own maven also serves mcp_config, which legacyforge resolves while
    // building the Minecraft artifacts.
    maven {
        name = "MinecraftForge"
        url = uri("https://maven.minecraftforge.net/")
    }
    maven {
        name = "Curse Maven"
        url = uri("https://www.cursemaven.com")
        content { includeGroup("curse.maven") }
    }
    maven { name = "Modrinth"; url = uri("https://api.modrinth.com/maven") }
    maven {
        name = "Jared"
        url = uri("https://maven.blamejared.com/")
        content { includeGroup("mezz.jei") }
    }
}

// ModDevGradle registers its own "NeoForged Releases" repository, which is not
// reachable from every build machine. Restrict it to the artifacts only it
// serves so an unreachable host cannot fail the resolution of everything else
// (Forge itself, Curse Maven mod jars and Maven Central libraries all come from
// the repositories below).
repositories.configureEach {
    if (name.contains("NeoForged", ignoreCase = true)) {
        content {
            includeGroup("net.neoforged")
        }
    }
}

dependencies {
    // Forge 1.20.1 mod dependencies; legacyforge remaps them to the dev
    // workspace's official mappings.
    compileOnly("maven.modrinth:curios:$curios_version")
    compileOnly("maven.modrinth:timeless-and-classics-zero:$tacz_version")
    compileOnly("mezz.jei:jei-1.20.1-forge:$jei_version")
    // Compile-time API for the Embeddium/Sodium 0.5 chunk-render tint mixin.
    compileOnly("maven.modrinth:embeddium:$embeddium_version")


    // Nullability annotations used throughout common.
    compileOnly("org.jetbrains:annotations:24.0.1")

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("it.unimi.dsi:fastutil:8.5.12") {
        isTransitive = false
    }
}

legacyForge {
    version = "$minecraft_version-$forge_version"

    runs {
        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            systemProperty("forge.logging.console.level", "info")
        }
        create("client") {
            client()
            gameDirectory.set(file("runs/client"))
            systemProperty("forge.enabledGameTestNamespaces", mod_id)
        }
        create("server") {
            server()
            gameDirectory.set(file("runs/server"))
            systemProperty("forge.enabledGameTestNamespaces", mod_id)
            programArguments.add("--nogui")
        }
        create("data") {
            data()
            gameDirectory.set(file("runs/data"))
            programArguments.addAll(
                "--mod", mod_id, "--all",
                "--output", file("src/generated/resources/").absolutePath,
                "--existing", file("src/main/resources/").absolutePath,
            )
        }
    }

    mods {
        create(mod_id) {
            sourceSet(sourceSets.main.get())
        }
    }
}

mixin {
    config("$mod_id.mixins.json")
    config("$mod_id.embeddium.mixins.json")
}

// Mixin annotation processor: legacyforge's mixin extension only feeds the run
// configurations, so the compiler arguments that generate the refmap (and the
// official->SRG mappings `reobfJar` needs for it) are wired here.
dependencies {
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
}

val mixinRefmap = layout.buildDirectory.file("mixin/$mod_id.refmap.json")
val mixinCompilerArgs = providers.provider {
    listOf(
        "-AreobfTsrgFile=" + obfuscation.namedToSrgMappings.get().asFile.absolutePath,
        "-AoutTsrgFile=" + mixinRefmap.get().asFile.absolutePath + ".mappings.tsrg",
        "-AoutRefMapFile=" + mixinRefmap.get().asFile.absolutePath,
        "-AmappingTypes=tsrg",
        "-ApluginVersion=0.7",
        "-AdefaultObfuscationEnv=searge",
    )
}
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgumentProviders.add(object : CommandLineArgumentProvider {
        override fun asArguments(): Iterable<String> = mixinCompilerArgs.get()
    })
}

// The refmap belongs in the mod jar and on the dev runtime classpath.
sourceSets.named("main") { resources.srcDir(mixinRefmap.map { it.asFile.parentFile }) }
// Production runs discover mixin configs through the jar manifest (the dev run
// gets `--mixin.config` from legacyforge instead).
tasks.named<Jar>("jar") {
    manifest {
        attributes("MixinConfigs" to "$mod_id.mixins.json,$mod_id.embeddium.mixins.json")
    }
}

tasks.named<ProcessResources>("processResources") {
    // Only the refmap belongs in resources; the side-car mappings file is an
    // input of reobfJar.
    exclude("*.mappings.tsrg")
}

// legacyforge registers its own reobfJar task and wires it into `assemble`.

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
    val loaderSpecific = providers.gradleProperty("modrinth_project_forge").orNull?.trim().orEmpty()
    if (loaderSpecific.isNotEmpty()) loaderSpecific else providers.gradleProperty("modrinth_project").orNull?.trim().orEmpty()
}
val curseforgeProjectId = providers.provider {
    val loaderSpecific = providers.gradleProperty("curseforge_project_forge").orNull?.trim().orEmpty()
    if (loaderSpecific.isNotEmpty()) loaderSpecific else providers.gradleProperty("curseforge_project").orNull?.trim().orEmpty()
}

modrinth {
    token.set(modrinthToken)
    projectId.set(modrinthProjectId)
    versionNumber.set(provider { "${project.version}+forge" })
    versionName.set(provider { "$mod_name ${project.version} (Forge 1.20.1) [${releaseDist.get()}]" })
    versionType.set(releaseType)
    changelog.set(publishChangelog)
    uploadFile.set(tasks.named("jar"))
    gameVersions.add(minecraft_version)
    loaders.add("forge")
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
    description = "Publish Forge 1.20.1 artifact to CurseForge."
    apiToken = curseforgeToken.orNull

    val resolvedProjectId = curseforgeProjectId.orNull
    if (!resolvedProjectId.isNullOrBlank()) {
        val mainFile = upload(resolvedProjectId, tasks.named("jar"))
        mainFile.releaseType = releaseType.get()
        mainFile.changelog = publishChangelog.get()
        mainFile.changelogType = "markdown"
        mainFile.addGameVersion(minecraft_version)
        mainFile.addModLoader("Forge")
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
    description = "Publish Forge 1.20.1 artifact to Modrinth and CurseForge."
    dependsOn("modrinth", "publishCurseForge")
}
