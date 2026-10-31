plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    // [改动前] 只有 Android 相关插件，数据库驱动只能作为 app 的普通依赖
    // [改动后] 增加 Kotlin JVM 插件，让 driver-api / driver-* 作为独立 JVM 模块编译
    // [原因] 驱动层不依赖 Android，拆分后可独立测试，并为后续动态驱动包方案复用
    id("org.jetbrains.kotlin.jvm") version "1.9.22" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
}
