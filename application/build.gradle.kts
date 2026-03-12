plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":domain"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.springframework:spring-context")
    implementation("org.springframework.security:spring-security-crypto")
    implementation("io.projectreactor:reactor-core")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.springframework.security:spring-security-core")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}