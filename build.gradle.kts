plugins {
    id("org.jetbrains.intellij") version "0.4.10"
    kotlin("jvm") version "1.3.50"
    java
    id("org.owasp.dependencycheck") version "5.1.0"
}

group = "csense-idea"
version = "0.200"


intellij {
    updateSinceUntilBuild = false //Disables updating since-build attribute in plugin.xml
    setPlugins("kotlin", "java")
    version = "2019.2.+"
}


repositories {
    jcenter()
}

dependencies {
    implementation("csense.kotlin:csense-kotlin-jvm:0.0.21")
}

tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes("""
      <ul>
        <li>Fixed previously introduced bug, where extensions files would be marked untested, sorry :)</li>
        <li>More quickfixes</li>
        <li>Generates different test code for list & array types </li>
      </ul>
      """)
}


tasks.getByName("check").dependsOn("dependencyCheckAnalyze")

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}