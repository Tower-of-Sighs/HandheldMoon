import net.darkhax.curseforgegradle.TaskPublishCurseForge
import org.gradle.api.attributes.Attribute
import org.gradle.jvm.tasks.Jar
import org.gradle.language.jvm.tasks.ProcessResources
import java.util.Properties

plugins {
    id("java-library")
    id("net.neoforged.moddev")
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
val neoforge_version: String by project
val neoforge_loader_version_range: String by project
val mod_id: String by project
val mod_name: String by project
val license: String by project
val mod_author: String by project
val curios_version: String by project
val jei_version: String by project
val iris_version: String by project
val sodium_version: String by project
val sable_version: String by project
val sable_companion_version: String by project

base {
    archivesName.set("$mod_name-neoforge-$minecraft_version")
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
// files whose 26.1 APIs have a confirmed 1.21.1 difference. This target ships
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
            // 26.1 client rendering, screens and item models.
            "cc/sighs/handheldmoon/client/LightSourceHighlightRenderer.java",
            "cc/sighs/handheldmoon/client/screen/AbstractLightSourceConfigScreen.java",
            "cc/sighs/handheldmoon/client/screen/AbstractFlashlightItemConfigScreen.java",
            "cc/sighs/handheldmoon/client/screen/FlashlightItemConfigScreen.java",
            "cc/sighs/handheldmoon/client/screen/HandheldMoonConfigScreen.java",
            "cc/sighs/handheldmoon/client/screen/LightSourceConfigScreen.java",
            "cc/sighs/handheldmoon/client/renderer/FullMoonRenderer.java",
            "cc/sighs/handheldmoon/client/renderer/MoonlightLampRenderer.java",
            "cc/sighs/handheldmoon/client/renderer/item/MoonlightLampPoweredProperty.java",
            "cc/sighs/handheldmoon/api/raycone/impl/RayConeRendererImpl.java",
            // 26.1 block entities, entity, item and accessory items.
            "cc/sighs/handheldmoon/block/FullMoonBlockEntity.java",
            "cc/sighs/handheldmoon/block/MoonlightLampBlockEntity.java",
            "cc/sighs/handheldmoon/entity/FullMoonEntity.java",
            "cc/sighs/handheldmoon/item/MoonlightLampItem.java",
            "cc/sighs/handheldmoon/item/ColorLensItem.java",
            "cc/sighs/handheldmoon/item/FullMoonItem.java",
            "cc/sighs/handheldmoon/item/MoonlightWandItem.java",
            // 26.1 post-processing and dynamic-light mixins.
            "cc/sighs/handheldmoon/event/handler/EffectManager.java",
            "cc/sighs/handheldmoon/event/handler/ShaderEventHandler.java",
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
            // 26.1 key bindings and shader-pipeline bridge.
            "cc/sighs/handheldmoon/registry/ModKeyBindings.java",
            "cc/sighs/handheldmoon/spi/AbstractClientPlatformService.java",
            "cc/sighs/handheldmoon/util/IrisCompat.java",
            "cc/sighs/handheldmoon/dynamiclight/DynamicLightEntityTint.java",
        )
    }
    into(layout.buildDirectory.dir("generated/common-main"))
}

tasks.named("compileJava") { dependsOn("prepareCommonSources") }

tasks.named<Jar>("sourcesJar") {
    dependsOn("prepareCommonSources")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// 1.21.1 recipes use the legacy ingredient JSON shape, so the 26.1 common
// recipe folder is replaced by this target's own files.
val prepareCommonResources = tasks.register<Sync>("prepareCommonResources") {
    from("../../common/src/main/resources") {
        // 1.21.1 ships its own legacy-shaped recipes, block models, core
        // shaders and item renderers; the 26.1 variants must not leak into it.
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
        "neoforge_version" to neoforge_version,
        "neoforge_loader_version_range" to neoforge_loader_version_range,
        "license" to license,
        "mod_author" to mod_author,
        "description" to (sharedProperties.getProperty("description") ?: ""),
    )
    filesMatching(listOf("META-INF/neoforge.mods.toml", "*.mixins.json")) {
        expand(expandProps)
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
    maven { name = "Curios Api"; url = uri("https://maven.theillusivec4.top/") }
    maven {
        name = "Shedaniel"
        url = uri("https://maven.shedaniel.me")
    }
    maven {
        name = "RyanHCode"
        url = uri("https://maven.ryanhcode.dev/releases")
    }
}

val sodiumFrapi = configurations.create("sodiumFrapi") {
    isCanBeConsumed = false
    isCanBeResolved = true
}
val extractSodiumFrapi = tasks.register<Copy>("extractSodiumFrapi") {
    from({ sodiumFrapi.files.map { zipTree(it) } }) {
        include("META-INF/jarjar/fabric-*.jar")
        eachFile { path = name }
        includeEmptyDirs = false
    }
    into(layout.buildDirectory.dir("sodium-frapi"))
}
val sodiumFrapiJars = fileTree(layout.buildDirectory.dir("sodium-frapi")) { include("*.jar") }
sodiumFrapiJars.builtBy(extractSodiumFrapi)

dependencies {
    sodiumFrapi("maven.modrinth:sodium:$sodium_version")
    // Sodium's FRAPI QuadEmitter/QuadView interfaces live in its nested jars.
    compileOnly(sodiumFrapiJars)
    implementation("curse.maven:carry-on-274259:7393892")
    implementation("maven.modrinth:jei:$jei_version")
    implementation("maven.modrinth:curios:$curios_version")

    implementation("maven.modrinth:sodium:$sodium_version")
    implementation("curse.maven:sodium-extra-447673:5913377")
    implementation("maven.modrinth:iris:$iris_version")

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

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("it.unimi.dsi:fastutil:8.5.12")
}

neoForge {
    version = neoforge_version

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
        }
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

// Implement mcgradleconventions loader attribute
val loaderAttribute = Attribute.of("io.github.mcgradleconventions.loader", String::class.java)
listOf("apiElements", "runtimeElements", "sourcesElements").forEach { variant ->
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
    val loaderSpecific = providers.gradleProperty("modrinth_project_neoforge").orNull?.trim().orEmpty()
    if (loaderSpecific.isNotEmpty()) loaderSpecific else providers.gradleProperty("modrinth_project").orNull?.trim().orEmpty()
}
val curseforgeProjectId = providers.provider {
    val loaderSpecific = providers.gradleProperty("curseforge_project_neoforge").orNull?.trim().orEmpty()
    if (loaderSpecific.isNotEmpty()) loaderSpecific else providers.gradleProperty("curseforge_project").orNull?.trim().orEmpty()
}

modrinth {
    token.set(modrinthToken)
    projectId.set(modrinthProjectId)
    versionNumber.set(provider { "${project.version}+neoforge" })
    versionName.set(provider { "$mod_name ${project.version} (NeoForge 1.21.1) [${releaseDist.get()}]" })
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
    description = "Publish NeoForge 1.21.1 artifact to CurseForge."
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
        !curseforgeToken.orNull.isNullOrBlank() && !curseforgeProjectId.orNull.isNullOrBlank()
    }
}

tasks.register("publishToPlatformServices") {
    group = "publishing"
    description = "Publish NeoForge 1.21.1 artifact to Modrinth and CurseForge."
    dependsOn("modrinth", "publishCurseForge")
}
