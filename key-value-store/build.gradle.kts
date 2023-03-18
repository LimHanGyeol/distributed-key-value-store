plugins {
    id("org.jlleitschuh.gradle.ktlint")

    kotlin("jvm") version "1.7.20"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation(kotlin("test"))
}
