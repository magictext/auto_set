
plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.13.3"
}

group = "org.intellij.sdk"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

// See https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.2")
    plugins.set(listOf("com.intellij.java"))
}

dependencies {
    // ...其他依赖项
//    compileOnly("org.projectlombok:lombok:1.18.20")
//    implementation("org.slf4j:slf4j-api:1.7.32")
//    runtimeOnly("org.slf4j:slf4j-simple:1.7.32")
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    patchPluginXml {
        sinceBuild.set("213")
        untilBuild.set("223.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
