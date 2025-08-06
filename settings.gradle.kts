plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "KspPluginTest"
include("annotations")
include("processor")
include("app")