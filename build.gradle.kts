@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.LanguageSettingsBuilder
import javax.xml.parsers.DocumentBuilderFactory

plugins {
    kotlin("multiplatform") version "2.3.0"
    id("org.jetbrains.dokka") version "2.1.0"
    id("org.jetbrains.kotlinx.kover") version "0.9.4"
    id("com.vanniktech.maven.publish") version "0.35.0"
}

repositories {
    mavenCentral()
    google()
}

group = "com.varabyte.truthish"
version = "2.0.0-SNAPSHOT"

/**
 * Extract the line coverage calculation from a generated Kover XML report.
 *
 * It is expected that someone will call the `koverReportXml` task and set its output into [koverReportFile].
 */
abstract class PrintLineCoverageFromKoverTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val koverReportFile: RegularFileProperty

    @TaskAction
    fun run() {
        val report = koverReportFile.get().asFile

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

val koverXmlReportTask = tasks.named("koverXmlReport")
tasks.register<PrintLineCoverageFromKoverTask>("printLineCoverage") {
    group = "verification"
    dependsOn(koverXmlReportTask)
    koverReportFile.set(layout.buildDirectory.file("reports/kover/report.xml"))
}

fun LanguageSettingsBuilder.setToVersion(version: String) {
    check(version.count { it == '.' } == 2)
    val withoutPatch = version.substringBeforeLast('.')
    languageVersion = withoutPatch
    apiVersion = withoutPatch
}

kotlin {
    compilerOptions {
        // By default, returned assertion values should be fluently chained, not ignored. Exceptions will be annotated
        // explicitly.
        // See also: https://kotlinlang.org/docs/unused-return-value-checker.html
        freeCompilerArgs.add("-Xreturn-value-checker=full")
    }

    jvm {
        compilerOptions {
            // We used to support JDK8 but support for it is getting dropped, e.g. on GitHub CIs, so moved to JDK11
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    js {
        browser()
        nodejs()
    }

    linuxArm64()
    linuxX64()
    macosArm64() // Mac M1+
    macosX64() // Mac Intel
    mingwX64() // Windows
    iosArm64() // iOS M1+
    iosX64() // iOS Intel
    iosSimulatorArm64()

    sourceSets {
        // For "Unused return value checker" functionality
        val targetKotlinVersion = "2.3.0"

        all {
            languageSettings {
                setToVersion(targetKotlinVersion)
            }
        }

        commonMain.dependencies {
            implementation(kotlin("stdlib", targetKotlinVersion))
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

val gcloudSecret: String? get() = (findProperty("gcloud.artifact.registry.secret") as? String)

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
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
