plugins {
    id("java-library")
    id("org.allaymc.gradle.plugin") version "0.2.1"
}

group = "me.daoge.aconomy"
description = "Aconomy is an simple implementation of https://github.com/AllayMC/EconomyAPI"
version = "0.1.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

allay {
    api = "0.18.0"

    plugin {
        entrance = ".Aconomy"
        authors += "daoge_cmd"
        website = "https://github.com/smartcmd/Aconomy"
    }
}

dependencies {
    compileOnly(group = "org.projectlombok", name = "lombok", version = "1.18.34")
    annotationProcessor(group = "org.projectlombok", name = "lombok", version = "1.18.34")
}
