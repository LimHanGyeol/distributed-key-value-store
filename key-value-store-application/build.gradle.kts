plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")

    kotlin("plugin.spring")
}

tasks.getByPath("bootJar").enabled = false
tasks.getByPath("jar").enabled = false

dependencies {
    implementation(project(":key-value-store"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
}
