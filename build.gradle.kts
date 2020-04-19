plugins {
    id("org.jetbrains.intellij") version "0.4.18"
    kotlin("jvm") version "1.3.71"
    java
    id("org.owasp.dependencycheck") version "5.2.4"
}

group = "csense-idea"
version = "0.250"


// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    updateSinceUntilBuild = false //Disables updating since-build attribute in plugin.xml
    setPlugins("Kotlin", "java") // "java" if taget 192 and above in plugin.xml
    version = "2019.2"
}


repositories {
    jcenter()
    maven(url = "https://dl.bintray.com/csense-oss/maven")
    maven(url = "https://dl.bintray.com/csense-oss/idea")
}

dependencies {
    implementation("csense.kotlin:csense-kotlin-jvm:0.0.31")
    implementation("csense.kotlin:csense-kotlin-annotations-jvm:0.0.17")
    implementation("csense.kotlin:csense-kotlin-ds-jvm:0.0.24")
    implementation("csense.idea.base:csense-idea-base:0.1.9")
}


tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes("""
      <ul>
        <li>Added coverage tool window</li>
      </ul>
      """)
}


tasks.getByName("check").dependsOn("dependencyCheckAnalyze")

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs = listOf("-progressive")
}

tasks.withType<JavaCompile>{
    targetCompatibility = "1.8"
    sourceCompatibility = "1.8"
}