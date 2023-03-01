pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "littlekt-extras"
include("core")
include("samples")

enableFeaturePreview("VERSION_CATALOGS")