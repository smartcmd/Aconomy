plugins {
    id("java-library")
    id("org.allaymc.gradle.plugin") version "0.2.1"
    id("com.gradleup.shadow") version "9.2.2"
}

group = "me.daoge.aconomy"
description = "Aconomy is an simple implementation of https://github.com/AllayMC/EconomyAPI"
version = "0.1.2-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

allay {
    api = "0.19.0"

    plugin {
        entrance = ".Aconomy"
        authors += "daoge_cmd"
        website = "https://github.com/smartcmd/Aconomy"
        dependency(name = "EconomyAPI", version = ">=0.2.0")
    }
}

dependencies {
    compileOnly(group = "org.projectlombok", name = "lombok", version = "1.18.34")
    compileOnly(group = "org.allaymc", name = "economy-api", version = "0.2.0")
    implementation(group = "org.xerial", name = "sqlite-jdbc", version = "3.47.1.0")
    implementation(group = "com.h2database", name = "h2", version = "2.4.240")
    annotationProcessor(group = "org.projectlombok", name = "lombok", version = "1.18.34")
}

tasks.shadowJar {
    archiveClassifier.set("shaded")
    mergeServiceFiles()
}
