pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// [改动前] 仓库同时存在 settings.gradle 与 settings.gradle.kts，Gradle 优先加载 Groovy 版本；
//          该版本没有 pluginManagement 仓库，导致 com.android.application 插件无法解析
// [改动后] 删除重复的 settings.gradle，本文件成为唯一 settings 配置
// [原因] 避免两套 settings 不一致；插件与依赖仓库统一由本文件的 google()/mavenCentral() 提供
// [改动前] rootProject.name = "RemoteDBExplorer"
// [改动后] rootProject.name = "RemoteDBClient"，模块结构不变（仍只有 :app）
// [原因] 根项目名与新的应用名保持一致，避免 IDE 与构建产物使用旧名字
// [改动前] 只有 :app 模块
// [改动后] 增加 driver-api、driver-mysql、driver-postgresql
// [原因] 把数据库方言与 JDBC 驱动从 app 核心中解耦，新增数据库以新增驱动模块为主
rootProject.name = "RemoteDBClient"
include(":app", ":driver-api", ":driver-mysql", ":driver-postgresql")
