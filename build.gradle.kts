plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.0"
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "com.cybrosys"
version = "1.3.6"

repositories {
    mavenCentral()
}

intellij {
    version.set("2024.2")
    type.set("PY")
    plugins.set(listOf("PythonCore", "python-ce"))
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("242")
        untilBuild.set("251.*")
    }
}
