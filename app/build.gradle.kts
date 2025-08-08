plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp") version "2.1.21-2.0.1"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(project(":annotations"))
    ksp(project(":processor"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("io.insert-koin:koin-core:3.5.3")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}