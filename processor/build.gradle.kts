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
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
//tasks.withType<Jar> {
//    manifest {
//        attributes["SymbolProcessorProvider"] = "com.example.processor.ValidateProcessorProvider"
//    }
//}