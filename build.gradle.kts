plugins {
    //https://plugins.gradle.org/plugin/org.jetbrains.intellij
    id("org.jetbrains.intellij") version "1.15.0"
    kotlin("jvm") version "1.9.0"
    //https://github.com/jeremylong/DependencyCheck (https://plugins.gradle.org/plugin/org.owasp.dependencycheck)
    id("org.owasp.dependencycheck") version "8.3.1"
}

group = "csense-idea"
version = "0.300"

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    updateSinceUntilBuild.set(false)
    plugins.set(listOf("Kotlin", "java"))
    version.set("2021.3")
}

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://pkgs.dev.azure.com/csense-oss/csense-oss/_packaging/csense-oss/maven/v1")
        name = "csense-oss"
    }
}
dependencies {
    implementation("csense.kotlin:csense-kotlin-jvm:0.0.60")
    implementation("csense.kotlin:csense-kotlin-annotations-jvm:0.0.63")
    implementation("csense.kotlin:csense-kotlin-datastructures-algorithms:0.0.41")
    implementation("csense.idea.base:csense-idea-base:0.1.60")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    testImplementation("csense.kotlin:csense-kotlin-tests:0.0.60")
    testImplementation("csense.idea.test:csense-idea-test:0.3.0")
}


tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes.set(
        """
      <ul>
        <li></li>
      </ul>
      """
    )
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.freeCompilerArgs = listOf("-progressive", "-opt-in=kotlin.contracts.ExperimentalContracts")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }

}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}