plugins {
    id("org.jetbrains.intellij") version "0.4.10"
    kotlin("jvm") version "1.3.50"
    java
    id("org.owasp.dependencycheck") version "5.1.0"
}

group = "csense-idea"
version = "0.8"


intellij {
    updateSinceUntilBuild = false //Disables updating since-build attribute in plugin.xml
    setPlugins("kotlin", "java")
    version = "2019.2.3"
}


repositories {
    jcenter()
}

dependencies {
    compile("csense.kotlin:csense-kotlin-jvm:0.0.21")
}

tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes("""
      <ul>
        <li>Now respects tests with types in the name and parameter names for overloads. </li>
        <li>Will not mark abstract functions as missing tests. </li>
        <li>Recognizes tests for extensions prefixed with the type name </li>
        <li>Add test method now works for newer idea versions. </li>
        <li>Add inspection for properties with custom setter / getter </li>
        <li>Requires exact match of naming since otherwise alike names would "be wrongly counted" (eg "startWith", and "startWithAny" could be unmarked by creating a "startWithAny" function..)</li>
      </ul>
      """)
}


tasks.getByName("check").dependsOn("dependencyCheckAnalyze")

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}