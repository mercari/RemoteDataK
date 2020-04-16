buildscript {

    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath(kotlin(Classpath.kotlin, Version.kotlin))
        classpath(Classpath.android)
        classpath(Classpath.androidJunit5)
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
