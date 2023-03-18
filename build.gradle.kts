import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"

    id("org.jlleitschuh.gradle.ktlint") version "11.2.0" apply false
}

allprojects {
    apply {
        plugin("kotlin")
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("org.jetbrains.kotlin.jvm")
    }

    group = "com.tommy"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

        testImplementation(kotlin("test"))
        testImplementation("org.assertj:assertj-core:3.24.2")
    }
}

subprojects {
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()

        reports.html.required.set(false)
        reports.junitXml.required.set(false)
    }
}
