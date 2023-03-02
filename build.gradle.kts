import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    kotlin("multiplatform") version "1.8.10"
    id("org.jetbrains.dokka") version "1.7.20"
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.2.0"
}

repositories {
    mavenCentral()
}

group = "com.varabyte.truthish"
version = "0.6.4-SNAPSHOT"

kotlin {
    jvm {
        val main by compilations.getting {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    js(IR) {
        browser()
    }

    linuxX64()
    macosArm64() // Mac M1
    macosX64() // Mac Intel
    mingwX64() // Windows

    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
            }
        }

        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }
        val jsMain by getting
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }

        val linuxX64Main by getting
        val macosArm64Main by getting
        val macosX64Main by getting
        val mingwX64Main by getting
    }
}

fun shouldSign() = (findProperty("truthish.sign") as? String).toBoolean()
fun shouldPublishToGCloud(): Boolean {
    return (findProperty("truthish.gcloud.publish") as? String).toBoolean()
            && findProperty("gcloud.artifact.registry.secret") != null
}
fun shouldPublishToMavenCentral(): Boolean {
    return (findProperty("truthish.maven.publish") as? String).toBoolean()
            && findProperty("ossrhUsername") != null && findProperty("ossrhPassword") != null
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
        username = findProperty("ossrhUsername") as String
        password = findProperty("ossrhPassword") as String
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
            (findProperty("ossrhUsername") as? String)?.let { username.set(it) }
            (findProperty("ossrhPassword") as? String)?.let { password.set(it) }
        }
    }
}
