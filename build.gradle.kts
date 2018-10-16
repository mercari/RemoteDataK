buildscript {

    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", extra.get("kotlinVersion") as String))
        classpath("com.novoda:bintray-release:${extra.get("bintrayReleaseVersion")}")
        classpath("org.junit.platform:junit-platform-gradle-plugin:1.2.0")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven { setUrl("https://dl.bintray.com/spekframework/spek-dev") }
    }
}

task("clean", Delete::class) {
    delete = setOf(rootProject.buildDir)
}
