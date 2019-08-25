plugins {
    id("org.jetbrains.intellij") version "0.4.10"
    kotlin("jvm") version "1.3.50"
    java
    id("org.owasp.dependencycheck") version "5.1.0"
}

group = "csense-idea"
version = "0.1"


intellij {
    updateSinceUntilBuild = false //Disables updating since-build attribute in plugin.xml
    setPlugins("kotlin")
    version = "2018.2"
}


repositories {
    jcenter()
}

dependencies {
    compile("csense.kotlin:csense-kotlin-jvm:0.0.21")
}

tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes("""
      First version only includes 2 simple inspections. This is mostly to make it obvious when testing things what is missing.<br/> More will come later, 
      for example some preliminary test code generation and or help. and more "intelligent" inspections.<br/> 
      For now this only works on classes and not extensions for example. 
      """)
}


tasks.getByName("check").dependsOn("dependencyCheckAnalyze")
