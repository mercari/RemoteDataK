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

tasks.create<Wrapper>("wrapper") {
   gradleVersion = "4.10.2"
}
