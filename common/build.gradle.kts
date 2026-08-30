plugins {
    `java-library`
}

val java_version: String by project

java {
    toolchain.languageVersion = JavaLanguageVersion.of(java_version.toInt())
    sourceCompatibility = JavaVersion.toVersion(java_version)
    targetCompatibility = JavaVersion.toVersion(java_version)
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.1.0")
    compileOnly("it.unimi.dsi:fastutil:8.5.12")
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("it.unimi.dsi:fastutil:8.5.12")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
