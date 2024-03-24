pluginManagement {
    repositories {
        maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
        gradlePluginPortal()
        google()
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "littlekt-extras"
include("core")
include("samples")