import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    kotlin("multiplatform") version "1.6.20"
    id("org.jetbrains.dokka") version "1.6.20"
    `maven-publish`
    signing
}

repositories {
    mavenCentral()
}

group = "com.varabyte.truthish"
version = "0.6.3-SNAPSHOT"

kotlin {
    jvm {
        val main by compilations.getting {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    js(BOTH) {
        browser()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

fun shouldSign() = (findProperty("truthish.sign") as? String).toBoolean()
fun shouldPublishToGCloud(): Boolean {
    return (findProperty("truthish.gcloud.publish") as? String).toBoolean()
            && findProperty("gcloud.artifact.registry.secret") != null
}
fun shouldPublishToMavenCentral(): Boolean {
    // Only publish snapshots to our varabyte repo for now, we may change our mind later
    return !version.toString().endsWith("SNAPSHOT")
            && (findProperty("truthish.maven.publish") as? String).toBoolean()
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
    signing {
        // Signing requires following steps at https://docs.gradle.org/current/userguide/signing_plugin.html#sec:signatory_credentials
        // and adding singatory properties somewhere reachable, e.g. ~/.gradle/gradle.properties
        sign(publishing.publications)
    }
}