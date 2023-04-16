plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")

    kotlin("plugin.spring")
}

tasks.getByPath("bootJar").enabled = false
tasks.getByPath("jar").enabled = true

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.redisson:redisson-spring-boot-starter:3.20.1")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("com.google.guava:guava:31.1-jre")

    implementation("io.micrometer:micrometer-core")
    implementation("io.micrometer:micrometer-registry-prometheus")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testImplementation("com.appmattus.fixture:fixture:1.2.0")
}
