import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.30"
}

group = "com.hyosakura"
version = "1.0"

repositories {
    google()
    mavenCentral()
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.name.startsWith("kotlin-")) {
            useVersion(getKotlinPluginVersion())
        }
    }
}

// Log
dependencies {
    implementation("org.fusesource.jansi:jansi:2.3.4")
    implementation("org.apache.logging.log4j:log4j-api:2.14.1")
    implementation("org.apache.logging.log4j:log4j-core:2.14.1")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.14.1")
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.5")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.12.5")
}


dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.5.30")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "16"
}
