import org.jetbrains.dokka.gradle.DokkaTask
import javax.xml.parsers.DocumentBuilderFactory

plugins {
    // NOTE: Intentionally older version to maximize compatibility with projects in the wild
    // Going much older and this buildscript won't compile
    kotlin("multiplatform") version "1.8.22"
    id("org.jetbrains.dokka") version "1.9.20"
    id("org.jetbrains.kotlinx.kover") version "0.8.3"
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("com.android.library") version "8.3.0"
}

repositories {
    mavenCentral()
    google()
}

group = "com.varabyte.truthish"
version = "1.0.2-SNAPSHOT"

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

kotlin {
    jvmToolchain(11) // Used by Android to set JDK version used by kotlin compilation

    jvm {
        val main by compilations.getting {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }
    js(IR) {
        browser()
        nodejs()
    }

    linuxX64()
    macosArm64() // Mac M1+
    macosX64() // Mac Intel
    mingwX64() // Windows
    iosX64() // iOS Intel
    iosArm64() // iOS M1+
    iosSimulatorArm64()
    android {
        publishLibraryVariants("release")
    }

    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
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

fun shouldSign() = (findProperty("truthish.sign") as? String).toBoolean()
fun shouldPublishToGCloud(): Boolean {
    return (findProperty("truthish.gcloud.publish") as? String).toBoolean()
            && findProperty("gcloud.artifact.registry.secret") != null
}
fun shouldPublishToMavenCentral(): Boolean {
    return (findProperty("truthish.maven.publish") as? String).toBoolean()
            && findProperty("ossrhToken") != null && findProperty("ossrhTokenPassword") != null
}


val VARABYTE_REPO_URL = uri("https://us-central1-maven.pkg.dev/varabyte-repos/public")
fun MavenArtifactRepository.gcloudAuth() {
    url = VARABYTE_REPO_URL
    credentials {
        username = "_json_key_base64"
        password = findProperty("gcloud.artifact.registry.secret") as String
    }
    authentication {
        create<BasicAuthentication>("basic")
    }
}

val SONATYPE_RELEASE_REPO_URL = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
val SONATYPE_SNAPSHOT_REPO_URL = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
fun MavenArtifactRepository.sonatypeAuth() {
    url = if (!version.toString().endsWith("SNAPSHOT")) SONATYPE_RELEASE_REPO_URL else SONATYPE_SNAPSHOT_REPO_URL
    credentials {
        username = findProperty("ossrhToken") as String
        password = findProperty("ossrhTokenPassword") as String
    }
    authentication {
        create<BasicAuthentication>("basic")
    }
}


repositories {
    mavenCentral()
}

val dokkaHtml by tasks.getting(DokkaTask::class)

val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaHtml.outputDirectory)
}

publishing {
    publications {
        if (shouldSign()) {
            if (shouldPublishToGCloud()) {
                repositories {
                    maven {
                        name = "GCloudMaven"
                        gcloudAuth()
                    }
                }
            }
            if (shouldPublishToMavenCentral()) {
                repositories {
                    maven {
                        name = "SonatypeMaven"
                        sonatypeAuth()
                    }
                }
            }
        }

        withType<MavenPublication> {
            artifact(javadocJar)
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
                    }
                }
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
            }
        }
    }
}

if (shouldSign()) {
    // If "shouldSign" returns true, then singing password should be set
    val secretKeyRingExists = (findProperty("signing.secretKeyRingFile") as? String)
        ?.let { File(it).exists() }
        ?: false

    // If "shouldSign" returns true, then singing password should be set
    val signingPassword = findProperty("signing.password") as String

    signing {
        // If here, we're on a CI. Check for the signing key which must be set in an environment variable.
        // See also: https://docs.gradle.org/current/userguide/signing_plugin.html#sec:in-memory-keys
        if (!secretKeyRingExists) {
            val signingKey: String? by project
            useInMemoryPgpKeys(signingKey, signingPassword)
        }

        // Signing requires following steps at https://docs.gradle.org/current/userguide/signing_plugin.html#sec:signatory_credentials
        // and adding singatory properties somewhere reachable, e.g. ~/.gradle/gradle.properties
        sign(publishing.publications)
    }
}

nexusPublishing {
    repositories {
        sonatype {  //only for users registered in Sonatype after 24 Feb 2021
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            (findProperty("ossrhToken") as? String)?.let { username.set(it) }
            (findProperty("ossrhTokenPassword") as? String)?.let { password.set(it) }
        }
    }
}
