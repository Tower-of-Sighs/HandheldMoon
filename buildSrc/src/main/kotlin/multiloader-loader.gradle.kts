plugins {
    id("multiloader-common")
}

val commonProject = project(":common")

dependencies {
    implementation(commonProject)
}
