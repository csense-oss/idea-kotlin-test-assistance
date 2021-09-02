plugins {
    id("org.jetbrains.intellij") version "1.1.4"
    kotlin("jvm") version "1.5.30"
    java
    id("org.owasp.dependencycheck") version "6.2.2"
}

group = "csense-idea"
version = "0.280"


// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    updateSinceUntilBuild.set(false)
    plugins.set(listOf("Kotlin", "java"))
    version.set("2020.3")
}

repositories {
    mavenCentral()
    //https://dev.azure.com/csense-oss/csense-oss/
    maven(url = "https://pkgs.dev.azure.com/csense-oss/csense-oss/_packaging/csense-oss/maven/v1")
}

dependencies {
    implementation("csense.kotlin:csense-kotlin-jvm:0.0.46")
    implementation("csense.kotlin:csense-kotlin-annotations-jvm:0.0.41")
    implementation("csense.kotlin:csense-kotlin-datastructures-algorithms:0.0.41")
    implementation("csense.idea.base:csense-idea-base:0.1.30")
}


tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes.set(
        """
      <ul>
        <li>More improvements / exception fixes</li>
        <li>Coverage now respects suppression</li>
        <li>Coverage now ignores anonymous classes</li>
      </ul>
      """
    )
}


tasks.getByName("check").dependsOn("dependencyCheckAnalyze")

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs = listOf("-progressive")
}

tasks.withType<JavaCompile> {
    targetCompatibility = "1.8"
    sourceCompatibility = "1.8"
}