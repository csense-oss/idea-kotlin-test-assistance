plugins {
    //https://plugins.gradle.org/plugin/org.jetbrains.intellij
    id("org.jetbrains.intellij") version "1.17.4"
    kotlin("jvm") version "2.0.20"
    //https://github.com/jeremylong/DependencyCheck (https://plugins.gradle.org/plugin/org.owasp.dependencycheck)
    id("org.owasp.dependencycheck") version "10.0.4"
}

group = "csense-idea"
version = "0.300"
// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    updateSinceUntilBuild.set(false)
    plugins.set(listOf("Kotlin", "java"))
    version.set("2023.2")
}

//tasks.getByName<org.jetbrains.intellij.tasks.RunIdeTask>("runIde") {
//    ideDir.set(File("/home/kasper/.local/share/JetBrains/Toolbox/apps/AndroidStudio/ch-0/223.8836.35.2231.10406996"))
//}


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
    implementation("csense.idea.base:csense-idea-base:0.1.71")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
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
        languageVersion.set(JavaLanguageVersion.of(17))
    }

}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}