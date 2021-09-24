plugins {
    kotlin("multiplatform") version "1.5.0"
    `maven-publish`
    signing
}

repositories {
    mavenCentral()
}

group = "com.varabyte.truthish"
version = "0.6.0"

kotlin {
    jvm {
        val main by compilations.getting {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    js {
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

repositories {
    mavenCentral()
    if (shouldPublishToGCloud()) {
        maven { gcloudAuth() }
    }
}

fun artifactSuffix(name: String): String {
    return when(name) {
        // For multiplatform targets, append them, so e.g. "kobweb" becomes "kobweb-js"
        "js", "jvm" -> "-${name}"
        else -> ""
    }
}

publishing {
    publications {
        if (shouldSign() && shouldPublishToGCloud()) {
            repositories {
                maven { gcloudAuth() }
            }
        }

        create<MavenPublication>("truthish") {
            this.artifactId = artifactId + artifactSuffix(name)
            pom {
                description.set("A reimplementation of Google Truth in Kotlin, so it can be used in Kotlin multi-platform projects")
                url.set("https://github.com/varabyte/truthish")
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
        sign(publishing.publications["truthish"])
    }
}