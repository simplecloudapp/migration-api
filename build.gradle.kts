plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.sonatype.central.portal.publisher)
    `maven-publish`
}

val baseVersion = "0.0.1"
val commitHash = System.getenv("COMMIT_HASH")
val snapshotVersion = "${baseVersion}-dev.$commitHash"

group = "app.simplecloud"
version = if (commitHash != null) snapshotVersion else baseVersion

repositories {
    mavenCentral()
}

dependencies {
    implementation(rootProject.libs.kotlin.jvm)
    implementation(rootProject.libs.bundles.configurate)
    implementation(rootProject.libs.bundles.log4j)
}

kotlin {
    jvmToolchain(21)
}

publishing {
    repositories {
        maven {
            name = "simplecloud"
            url = uri("https://repo.simplecloud.app/snapshots/")
            credentials {
                username = System.getenv("SIMPLECLOUD_USERNAME")?: (project.findProperty("simplecloudUsername") as? String)
                password = System.getenv("SIMPLECLOUD_PASSWORD")?: (project.findProperty("simplecloudPassword") as? String)
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}

signing {
    if (commitHash != null) {
        return@signing
    }

    sign(publishing.publications)
    useGpgCmd()
}