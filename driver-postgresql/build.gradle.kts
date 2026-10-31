// driver-postgresql：PostgreSQL 方言与 JDBC 驱动；只依赖 driver-api。
plugins {
    id("org.jetbrains.kotlin.jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "17"
}

dependencies {
    implementation(project(":driver-api"))
    implementation("org.postgresql:postgresql:42.6.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.testcontainers:postgresql:1.21.3")
    testImplementation("org.slf4j:slf4j-simple:2.0.17")
}
