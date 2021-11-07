plugins {
    id("java-library")
    kotlin("jvm")
    id("com.github.dcendents.android-maven")
}

dependencies {
    // kotlin
    implementation(Configuration.Dependencies.kotlin_stdlib)

    // 测试
    testImplementation(Configuration.Dependencies.test_junit)
}

