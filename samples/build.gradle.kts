import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType
import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

repositories {
    mavenCentral()
    maven(url = "https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

kotlin {
    tasks.withType<JavaExec> { jvmArgs("--enable-native-access=ALL-UNNAMED") }
    jvm {
        compilations {
            val main by getting

            val mainClassName = "com.lehaine.rune.PixelExampleKt"
            tasks {
                register<Copy>("copyResources") {
                    group = "package"
                    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                    dependsOn(named("jvmProcessResources"))
                    from(main.output.resourcesDir)
                    destinationDir = File("${layout.buildDirectory.asFile.get()}/publish")
                }
                register<Jar>("packageFatJar") {
                    group = "package"
                    archiveClassifier.set("all")
                    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                    dependsOn(named("jvmJar"))
                    dependsOn(named("copyResources"))
                    manifest { attributes["Main-Class"] = mainClassName }
                    destinationDirectory.set(File("${layout.buildDirectory.asFile.get()}/publish/"))
                    from(
                        main.runtimeDependencyFiles.map { if (it.isDirectory) it else zipTree(it) },
                        main.output.classesDirs
                    )
                    doLast {
                        logger.lifecycle(
                            "[LittleKt] The packaged jar is available at: ${outputs.files.first().parent}"
                        )
                    }
                }
                if (Os.isFamily(Os.FAMILY_MAC)) {
                    register<JavaExec>("jvmRun") {
                        jvmArgs("-XstartOnFirstThread")
                        mainClass.set(mainClassName)
                        kotlin {
                            val mainCompile = targets["jvm"].compilations["main"]
                            dependsOn(mainCompile.compileAllTaskName)
                            classpath(
                                { mainCompile.output.allOutputs.files },
                                (configurations["jvmRuntimeClasspath"])
                            )
                        }
                    }
                }
            }
        }
        compilerOptions { jvmTarget = JvmTarget.JVM_24 }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }
    js(KotlinJsCompilerType.IR) {
        binaries.executable()
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }

        this.attributes.attribute(
            KotlinPlatformType.attribute,
            KotlinPlatformType.js
        )

        compilerOptions {
            sourceMap = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":core"))
                implementation(libs.littlekt.core)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.fleks)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(project(":core"))
            }
        }
        val jvmTest by getting
        val jsMain by getting {
            dependencies {
                val kotlinxHtmlVersion = "0.11.0"
                implementation(project(":core"))
                implementation("org.jetbrains.kotlinx:kotlinx-html-js:$kotlinxHtmlVersion")
            }

        }
        val jsTest by getting

        all {
            languageSettings.apply {
                progressiveMode = true
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                optIn("kotlin.contracts.ExperimentalContracts")
                optIn("kotlin.ExperimentalUnsignedTypes")
                optIn("kotlin.ExperimentalStdlibApi")
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
                optIn("kotlin.time.ExperimentalTime")
            }
        }
    }
}