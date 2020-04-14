import org.gradle.kotlin.dsl.extra

object Artifact {
  const val groupdId = "com.mercari.remotedata"
  const val version = "1.0.0"
}

object MavenUrl {
  const val spekDev = "https://dl.bintray.com/spekframework/spek-dev"
}

object Version {
  const val android = "3.6.2"
  const val androidJunit5 = "1.6.0.0"
  const val bintray = "1.8.4"
  const val kotlin = "1.3.30"
  const val kluent = "1.60"
  const val spek = "2.0.10"
  const val junitPlatform = "1.2.0"
  const val junitEngine = "5.6.0"
  const val junit4 = "4.13"
  const val jacoco = "0.8.3"
}

object Classpath {
  const val android = "com.android.tools.build:gradle:${Version.android}"
  const val androidJunit5 = "de.mannodermaus.gradle.plugins:android-junit5:${Version.androidJunit5}"
  const val kotlin = "gradle-plugin"
  const val junitPlatform = "org.junit.platform:junit-platform-gradle-plugin:${Version.junitPlatform}"
  const val bintray = "com.jfrog.bintray.gradle:gradle-bintray-plugin:${Version.bintray}"
}

object Dependencies {
  const val kotlin = "stdlib"
}

object TestDependencies {
  const val kluent = "org.amshove.kluent:kluent-android:${Version.kluent}"
  const val junitJupiter = "org.junit.jupiter:junit-jupiter-api:${Version.junitEngine}"
  const val junitEngine = "org.junit.jupiter:junit-jupiter-engine:${Version.junitEngine}"
  const val spek = "org.spekframework.spek2:spek-dsl-jvm:${Version.spek}"
  const val spekRunner = "org.spekframework.spek2:spek-runner-junit5:${Version.spek}"
  const val junit4 = "junit:junit:${Version.junit4}"
  const val roboletric = "org.robolectric:robolectric:4.3.1"
}

object AndroidConfig {
  const val compileSdkVersion = 29
  const val minSdkVersion = 21
  const val targetSdkVersion = 29
}
