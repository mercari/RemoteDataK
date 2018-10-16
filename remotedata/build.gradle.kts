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

task<JacocoReport>("codeCoverageReport") {
    group = "reporting"

    val junitPlatformTest: JavaExec by tasks

    reports {
        xml.isEnabled = true
        xml.destination = file("${project.buildDir}/reports/jacoco/codeCoverageReport/report.xml")
        html.isEnabled = true
    }

    val tree = fileTree("${project.buildDir}/classes")

    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories = files(mainSrc)
    classDirectories = files(tree)

    executionData = fileTree(project.buildDir) {
        include("jacoco/*.exec")
    }

    dependsOn(junitPlatformTest)
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
