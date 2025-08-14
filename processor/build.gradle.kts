plugins {
    kotlin("jvm")
    `java-library`
    `maven-publish`
}
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.nemanjap"
            artifactId = "ksp-processor"
            version = "0.1.0"
            from(components["java"])
        }
    }
}

group = "com.nemanjap"
version = "0.1.0"

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