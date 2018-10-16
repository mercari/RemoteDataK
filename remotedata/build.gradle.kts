import com.novoda.gradle.release.PublishExtension

plugins {
    kotlin("jvm")

    jacoco
    id("org.junit.platform.gradle.plugin")

    id("com.novoda.bintray-release")
}

repositories {
    mavenCentral()
}

dependencies {
    val kotlinVersion = extra.get("kotlinVersion") as String
    implementation(kotlin("stdlib", kotlinVersion))

    // assertion
    testImplementation("org.amshove.kluent:kluent-android:${extra.get("kluentVersion") as String}")

    //spek2
    val spekVersion = extra.get("spekVersion") as String
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:$spekVersion")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:$spekVersion")
}

jacoco {
    toolVersion = "0.8.1"

    val junitPlatformTest: JavaExec by tasks
    applyTo(junitPlatformTest)
}

junitPlatform {
    filters {
        engines {
            include("spek2")
        }
    }
    enableStandardTestTask = true
}

tasks {
    "jacocoTestReport"(JacocoReport::class) {
        reports {
            html.isEnabled = false
            xml.isEnabled = true
        }
    }
}

configure<PublishExtension> {
    uploadName = "RemoteData"
    groupId = "com.mercari.remotedata"
    artifactId = "RemoteData"
    publishVersion = extra.get("publishVersion") as String
    autoPublish = true
    desc = "Abstract Data Type (ADT) to represent data that is fetching from the remote sources"
    website = "https://github.com/mercari/RemoteData"
}
