@file:OptIn(ExperimentalWasmDsl::class)

import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.LanguageSettingsBuilder
import javax.xml.parsers.DocumentBuilderFactory

plugins {
    kotlin("multiplatform") version "2.1.0"
    id("org.jetbrains.dokka") version "1.9.20"
    id("org.jetbrains.kotlinx.kover") version "0.8.3"
    id("com.android.library") version "8.3.0"
    id("com.vanniktech.maven.publish") version "0.32.0"
}

repositories {
    mavenCentral()
    google()
}

group = "com.varabyte.truthish"
version = "1.0.3-SNAPSHOT"

tasks.register("printLineCoverage") {
    group = "verification" // Put into the same group as the `kover` tasks
    dependsOn("koverXmlReport")
    doLast {
        val report = layout.buildDirectory.file("reports/kover/report.xml").get().asFile

        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(report)
        val rootNode = doc.firstChild
        var childNode = rootNode.firstChild

        var coveragePercent = 0.0

        while (childNode != null) {
            if (childNode.nodeName == "counter") {
                val typeAttr = childNode.attributes.getNamedItem("type")
                if (typeAttr.textContent == "LINE") {
                    val missedAttr = childNode.attributes.getNamedItem("missed")
                    val coveredAttr = childNode.attributes.getNamedItem("covered")

                    val missed = missedAttr.textContent.toLong()
                    val covered = coveredAttr.textContent.toLong()

                    coveragePercent = (covered * 100.0) / (missed + covered)

                    break
                }
            }
            childNode = childNode.nextSibling
        }

        println("%.1f".format(coveragePercent))
    }
}

fun LanguageSettingsBuilder.setToVersion(version: String) {
    check(version.count { it == '.' } == 2)
    val withoutPatch = version.substringBeforeLast('.')
    languageVersion = withoutPatch
    apiVersion = withoutPatch
}

kotlin {
    jvmToolchain(11) // Used by Android to set JDK version used by kotlin compilation

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    js {
        browser()
        nodejs()
    }
    wasmJs {
        browser()
        nodejs()
        d8()
    }

    linuxArm64()
    linuxX64()
    macosArm64() // Mac M1+
    macosX64() // Mac Intel
    mingwX64() // Windows
    iosArm64() // iOS M1+
    iosX64() // iOS Intel
    iosSimulatorArm64()

    androidTarget {
        publishLibraryVariants("release")
    }

    sourceSets {
        // Anything lower and: "API version 1.x is no longer supported; please, use version 1.6 or greater."
        // We also use "buildMap" from 1.6
        val targetKotlinVersion = "1.6.21"

        all {
            languageSettings {
                setToVersion(targetKotlinVersion)
            }
        }

        commonMain.dependencies {
            implementation(kotlin("stdlib", targetKotlinVersion))
        }

        wasmJsMain {
            // Wasm needs to use 1.9+ or else compilation dies.
            // This exception is not a big deal since Wasm didn't really exist until the end of 1.8 anyway
            val wasmKotlinVersion = "1.9.25"
            languageSettings {
                setToVersion(wasmKotlinVersion)
            }

            dependencies {
                implementation(kotlin("stdlib", wasmKotlinVersion))
            }
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "com.varabyte.truthish"
    compileSdk = 33

    compileOptions {
        // We used to support JDK8 but support for it is getting dropped, e.g. on GitHub CIs, so we're moving to JDK11
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

val gcloudSecret: String? get() = (findProperty("gcloud.artifact.registry.secret") as? String)

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    signAllPublications()

    pom {
        val githubPath = "https://github.com/varabyte/truthish"
        name.set("Truthish")
        description.set("A Google Truth inspired testing library written in Kotlin supporting multi-platform projects")
        url.set(githubPath)
        scm {
            url.set(githubPath)
            val connectionPath = "scm:git:${githubPath}.git"
            connection.set(connectionPath)
            developerConnection.set(connectionPath)
        }
        developers {
            developer {
                id.set("bitspittle")
                name.set("David Herman")
                email.set("bitspittle@gmail.com")
                url.set("https://github.com/bitspittle")
            }
        }
        licenses {
            license {
                name.set("Apache-2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
    }
}

repositories {
    mavenCentral()
}

publishing {
    publications {
        gcloudSecret?.let { gcloudSecret ->
            repositories {
                maven {
                    name = "GCloudMaven"
                    url = uri("https://us-central1-maven.pkg.dev/varabyte-repos/public")
                    credentials {
                        username = "_json_key_base64"
                        password = gcloudSecret
                    }
                    authentication {
                        create<BasicAuthentication>("basic")
                    }
                }
            }
        }
    }
}
