plugins {
    kotlin("jvm")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(project(":annotations"))
    implementation("com.google.devtools.ksp:symbol-processing-api:2.1.21-2.0.1")
    implementation("com.squareup:kotlinpoet:2.2.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}