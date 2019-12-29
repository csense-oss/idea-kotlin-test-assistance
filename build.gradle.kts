plugins {
    id("org.jetbrains.intellij") version "0.4.15"
    kotlin("jvm") version "1.3.61"
    java
    id("org.owasp.dependencycheck") version "5.1.0"
}

group = "csense-idea"
version = "0.230"


// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    updateSinceUntilBuild = false //Disables updating since-build attribute in plugin.xml
    setPlugins("Kotlin", "java") // "java" if taget 192 and above in plugin.xml
    version = "2019.2"
}


repositories {
    jcenter()
    //until ds is in jcenter
    maven(url = "https://dl.bintray.com/csense-oss/maven")
}

dependencies {
    implementation("csense.kotlin:csense-kotlin-jvm:0.0.27")
    implementation("csense.kotlin:csense-kotlin-annotations-jvm:0.0.14")
    implementation("csense.kotlin:csense-kotlin-ds-jvm:0.0.24")
}


tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes("""
      <ul>
        <li>Now works with common modules (better test naming strategy for module resolving)</li>
        <li>More fixes(eg names, more overloads ect)</li>
        <li>Handles anonymous objects much better now</li>
      </ul>
      <br/>
      Nb, will improve the naming schemes later (as overloaded extensions on complex types creates very long weird names).
      """)
}


tasks.getByName("check").dependsOn("dependencyCheckAnalyze")

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs = listOf("-progressive")
}