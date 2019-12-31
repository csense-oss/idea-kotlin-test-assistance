plugins {
    id("org.jetbrains.intellij") version "0.4.15"
    kotlin("jvm") version "1.3.61"
    java
    id("org.owasp.dependencycheck") version "5.2.4"
}

group = "csense-idea"
version = "0.232"


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
    implementation("csense.kotlin:csense-kotlin-jvm:0.0.29")
    implementation("csense.kotlin:csense-kotlin-annotations-jvm:0.0.14")
    implementation("csense.kotlin:csense-kotlin-ds-jvm:0.0.24")
}


tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes("""
      <ul>
        <li>Now works with common modules (better test naming strategy for module resolving)</li>
        <li>More fixes(eg names, more overloads ect)</li>
        <li>Handles anonymous objects much better now</li>
        <li>Now uses classes rather than objects (objects fails for junit)</li>
        <li>Sealed and abstract classes are not marked</li>
        <li>When creating test files, adds suppression to unused (so idea will not mark test methods / classes as unused)</li>
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