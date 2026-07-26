import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named

plugins {
    id("multiloader-common")
}

val commonProject = project(":common")
val loaderProject = project

dependencies {
    implementation(commonProject)
}

val loaderSourceSets = extensions.getByType<SourceSetContainer>()
loaderSourceSets.named("main") {
    java.srcDir(commonProject.file("src/minecraft/java"))
    resources.srcDir(commonProject.file("src/main/resources"))
}

commonProject.afterEvaluate {
    val commonSourceSets = extensions.getByType<SourceSetContainer>()
    loaderProject.tasks.named<Jar>("jar") {
        dependsOn(commonProject.tasks.named("classes"))
        from(commonSourceSets.named("main").map { it.output.classesDirs })
    }
    loaderProject.tasks.named<Jar>("sourcesJar") {
        from(commonSourceSets.named("main").map { it.java })
    }
}
