import org.gradle.kotlin.dsl.extra

object Artifact {
  const val groupdId = "com.mercari.remotedata"
  const val version = "1.0.1"
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

  const val rxjava = "3.0.2"
  const val rxandroid = "3.0.0"
  const val androidLifecycle = "2.2.0"
  const val androidAppCompat = "1.1.0"
  const val androidFragment = "1.2.4"
  const val androidConstraintLayout = "1.1.3"
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

  const val rxjava = "io.reactivex.rxjava3:rxjava:${Version.rxjava}"
  const val rxandroid = "io.reactivex.rxjava3:rxandroid:${Version.rxandroid}"
  const val androidAppCompat = "androidx.appcompat:appcompat:${Version.androidAppCompat}"
  const val androidFragment = "androidx.fragment:fragment-ktx:${Version.androidFragment}"
  const val androidLifecycle = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Version.androidLifecycle}"
  const val androidLiveData = "androidx.lifecycle:lifecycle-livedata-ktx:${Version.androidLifecycle}"
  const val androidSavedState = "androidx.lifecycle:lifecycle-viewmodel-savedstate:${Version.androidLifecycle}"
  const val androidLifecycleCompiler = "androidx.lifecycle:lifecycle-compiler:${Version.androidLifecycle}"
  const val androidConstraintLayout = "androidx.constraintlayout:constraintlayout:${Version.androidConstraintLayout}"
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
