plugins {
    id("org.jetbrains.intellij") version "0.5.0"
    kotlin("jvm") version "1.4.31"
    java
    id("org.owasp.dependencycheck") version "5.3.2"
}

group = "csense-idea"
version = "0.280"


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
    maven(url = "https://pkgs.dev.azure.com/csense-oss/_packaging/csense-oss/maven/v1")
}

dependencies {
    implementation("csense.kotlin:csense-kotlin-jvm:0.0.46-snapshot.1")
    testImplementation("csense.kotlin:csense-kotlin-jvm:0.0.46-snapshot.0")
    implementation("csense.kotlin:csense-kotlin-annotations-jvm:0.0.40")
    implementation("csense.kotlin:csense-kotlin-datastructures-algorithms:0.0.41")
    implementation("csense.idea.base:csense-idea-base:0.1.20")
}


tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes("""
      <ul>
        <li>More improvements / exception fixes</li>
      </ul>
      """)
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