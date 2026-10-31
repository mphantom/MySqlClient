# RemoteDB Client

Android 上的远程数据库客户端：在手机上连接并管理 **MySQL / PostgreSQL**，浏览表结构与数据、直接编辑单元格、执行自定义 SQL，并导出查询结果。交互思路类似桌面端的 Navicat。

- **应用名**：RemoteDB Client
- **包名 / applicationId**：`com.phoenix.remotedbclient`
- **版本**：1.0.0（versionCode 1）
- **系统要求**：Android 8.0+（minSdk 26），targetSdk 34

> 仓库目录名仍为 `MySqlClient`（历史原因），代码与构建配置中的应用名已统一为 RemoteDB Client。

## 功能

- **连接管理**：保存多个 MySQL / PostgreSQL 连接，支持新增、编辑、删除与连接测试。
- **密码安全存储**：密码使用 Android Keystore 支持的 `EncryptedSharedPreferences` 加密保存，不写入 Room 数据库。
- **结构浏览**：查看表 / 视图列表，以及列名、数据类型、主键、可空性、列序号。
- **数据浏览**：按 50 行/页分页查看表数据；点击列头切换升/降序，默认按主键排序保证分页稳定；点击单元格修改、长按行删除、浮动按钮新增行。
- **SQL 查询**：内置多行 SQL 编辑器、最近 20 条查询历史、结果表格；SELECT 结果可导出 CSV 并调用系统分享。
- **多连接工作区**：顶部标签页可同时打开多个连接，在 Browse / Query 两个面板间切换。
- **文案本地化**：界面与提示文案统一放在 `strings.xml`；Compose 使用 `stringResource`，ViewModel 通过 `UiText` 传递资源引用，数量文案使用 `plurals`。

## 技术栈

| 分类 | 选型 |
| --- | --- |
| 语言 / UI | Kotlin 1.9.22、Jetpack Compose（BOM 2024.01.00）、Material 3 |
| 架构 | 单 Activity + Navigation Compose、MVVM、Hilt 2.50 |
| 本地存储 | Room 2.6.1、EncryptedSharedPreferences（security-crypto） |
| 远程访问 | 设备内 JDBC，驱动按 `DatabaseDialect` 拆成独立模块（MySQL / PostgreSQL） |
| 异步 | Kotlin Coroutines 1.7.3（JDBC 操作统一走 `Dispatchers.IO`） |
| 构建 | Gradle 8.5、AGP 8.2.2、KSP、Java 17 |

## 项目结构

```
driver-api/                      # DatabaseDialect、DriverRegistry、TableInfo/ColumnInfo/QueryResult
driver-mysql/                    # MySqlDialect + MySQL Connector/J
driver-postgresql/               # PostgreSqlDialect + PostgreSQL JDBC
app/src/main/java/com/phoenix/remotedbclient/
├── RemoteDBClientApp.kt          # @HiltAndroidApp + MultiDexApplication
├── MainActivity.kt               # 单 Activity，加载 Compose 导航
├── di/AppModule.kt               # Room、DAO、方言 Set、DriverRegistry、连接管理器
├── data/
│   ├── db/                       # ConnectionConfigEntity / DAO / AppDatabase
│   ├── remote/                   # 连接生命周期、会话管理（方言在 driver-* 模块）
│   └── repository/               # ConnectionRepository
├── util/SecurePreferences.kt     # 密码加密存储
└── ui/
    ├── common/UiText.kt          # ViewModel → Compose 的文案资源模型
    ├── main/                     # 导航图 + 工作区标签页
    ├── connection/               # 连接列表、新增/编辑表单
    ├── browser/                  # 表结构 + 数据浏览
    ├── query/                    # SQL 编辑器 + 结果 + CSV 导出
    └── theme/                    # 颜色、字体、Material 3 主题
```

测试源码分别位于 `app/src/test/java/`（JVM 单元测试）与 `app/src/androidTest/java/`（instrumented 测试）。

分层关系：

```
Compose Screen → Hilt ViewModel → Repository / SessionManager
        → RemoteDatabaseManager → DriverRegistry
        → DatabaseDialect（driver-mysql / driver-postgresql）→ 远程数据库
```

## 驱动扩展

新增一种数据库不需要修改 `RemoteDatabaseManager` 或 Compose 页面：

1. 新建 `:driver-xxx` Kotlin JVM 模块，依赖 `:driver-api` 与对应 JDBC 驱动。
2. 实现 `DatabaseDialect`，通常继承 `AbstractJdbcDialect`，覆写 JDBC URL、分页语法、元数据查询与标识符引用。
3. 在 `settings.gradle.kts` 中 include 新模块，并在 `app/build.gradle.kts` 增加 `implementation(project(":driver-xxx"))`。
4. 在 `AppModule` 里加一个 `@Provides @IntoSet` 绑定。
5. 重新构建，数据库类型选择器会自动出现新方言。

当前仍是编译期模块，新增驱动需要重新打包 APK；运行时驱动包（方案 B）或桌面 Bridge（方案 C）属于后续演进。

## 构建

环境：JDK 17+、Android SDK 34（本次使用 JDK 21 + Gradle 8.5 验证通过）。

```bash
./gradlew assembleDebug        # Linux / macOS
gradlew.bat assembleDebug      # Windows
```

产物位于 `app/build/outputs/apk/debug/app-debug.apk`。

> 仓库只保留 `settings.gradle.kts`。不要再添加 `settings.gradle`：Gradle 会优先加载 Groovy 版本，导致 `pluginManagement` 仓库声明失效。

> 注意：`gradle/wrapper/gradle-wrapper.properties` 已指向官方 `gradle-8.5-bin.zip`；首次构建会下载 Gradle 与依赖。

## 测试

### JVM 单元测试

```bash
./gradlew testDebugUnitTest
```

- `app/src/test/java/`：`QueryResult`、`BrowserUiState`、`SessionManager`、`QueryViewModel`、`BrowserViewModel`、`ConnectionViewModel`，以及 `DriverRegistry` / 两种方言的 URL 与标识符转义，共 30 个用例。
- 无需设备、数据库或 Docker。

### 真实数据库契约测试（Docker）

```bash
bash scripts/wsl-driver-tests.sh
```

- `driver-mysql` / `driver-postgresql` 使用 Testcontainers 启动 `mysql:8.0` 和 `postgres:16-alpine`。
- 共 14 个用例：保留字表名/列名、引号转义、元数据、单列排序、分页、增删改、恶意表名注入防护。
- 需要 Docker Engine；Docker 29 要求最低 API 1.44，脚本已设置 `DOCKER_API_VERSION` 和 `-Dapi.version=1.44`。
- WSL 中通过 `settings-drivers.gradle.kts` 只构建驱动模块，不需要 Android SDK。
- 首次运行会拉取 Gradle 依赖和两个数据库镜像。

### Android instrumented 测试

```bash
./gradlew connectedDebugAndroidTest
```

- `app/src/androidTest/java/`：`ConnectionConfigDao` 的 Room 内存数据库增删改查与排序测试（4 个用例）。
- 当前环境没有已连接的设备/模拟器，instrumented 测试已编译通过，但尚未在设备上执行。

## 命名统一说明

本次将历史上的 `RemoteDBExplorer` / `com.example.remotedbexplorer` 统一为 RemoteDB Client：

| 项目 | 改动前 | 改动后 |
| --- | --- | --- |
| 应用名 | RemoteDB Explorer | RemoteDB Client |
| 包名 / applicationId | `com.example.remotedbexplorer` | `com.phoenix.remotedbclient` |
| Application 类 | `RemoteDBApp` | `RemoteDBClientApp` |
| Compose 主题 | `RemoteDBExplorerTheme` | `RemoteDBClientTheme` |
| XML 主题 | `Theme.RemoteDBExplorer` | `Theme.RemoteDBClient` |
| 根项目名 | RemoteDBExplorer | RemoteDBClient |
| Room 数据库文件 | `remotedb_explorer.db` | `remotedb_client.db` |
| 加密偏好文件 | `remotedb_secure_prefs` | `remotedb_client_prefs` |

由于 applicationId 已变更，新包会被系统视为独立应用，**旧版本的连接配置、密码与数据库不会自动迁移**；首次安装需要重新添加连接。

## 已知问题与后续工作

- **会话恢复**：连接会话只保存在进程内，应用被系统回收后需要重新连接；后续可持久化最近连接信息并做自动重连。
- **传输安全**：`network_security_config` 全局允许明文流量，MySQL URL 固定 `useSSL=false`，后续应提供 TLS 开关。
- **驱动扩展方式**：当前仍是编译期驱动模块，新增数据库需要重新打包 APK；运行时驱动包（方案 B）或桌面 Bridge（方案 C）尚未实现。
- **测试覆盖**：已加入 30 个 JVM 单元测试、14 个真实数据库契约用例与 4 个 Room DAO instrumented 用例；主链路已有 JVM 集成测试，Compose UI、SecurePreferences 与设备端 E2E 尚未覆盖。
- **排序能力**：目前是单列排序；多列排序、按表达式排序和索引提示尚未实现。
