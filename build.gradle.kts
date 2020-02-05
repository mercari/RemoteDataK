buildscript {

    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath(kotlin(Classpath.kotlin, Version.kotlin))
        classpath(Classpath.junitPlatform)
        classpath(Classpath.bintray)
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven { setUrl(MavenUrl.spekDev) }
    }
}
