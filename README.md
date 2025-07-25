# CryptoTrader

本项目是一个使用 Kotlin 编写的 Android 应用，采用 Jetpack Compose 构建界面，并结合 Hilt、Retrofit、Room 等库实现依赖注入、网络请求以及本地存储。

## 项目结构

```
CryptoTrader
├── app/                        # 主应用模块
│   ├── build.gradle.kts        # 模块级 Gradle 配置
│   ├── proguard-rules.pro      # ProGuard 混淆规则
│   └── src/                    # 源码
│       ├── androidTest/        # 仿真设备/集成测试代码
│       ├── main/               # 主源码与资源
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/cryptotrader
│       │   │   ├── MainActivity.kt        # 应用入口 Activity
│       │   │   ├── TradingApplication.kt  # Application 初始化
│       │   │   ├── data/                  # 数据层
│       │   │   │   ├── local/             # 本地数据库实体与 DAO
│       │   │   │   ├── remote/            # 网络仓库实现
│       │   │   │   └── ...                # 其他模型/仓库
│       │   │   ├── di/                    # Hilt 依赖注入模块
│       │   │   ├── domain/                # 业务用例层
│       │   │   │   └── usecase/           # 各种业务用例
│       │   │   └── ui/                    # Compose 界面与 ViewModel
│       │   │       ├── screens/           # 具体页面
│       │   │       │   ├── trade/
│       │   │       │   └── detail/
│       │   │       └── theme/             # 应用主题与样式
│       │   └── res/                       # 资源文件
│       └── test/                          # 单元测试代码
├── build.gradle.kts            # 项目级 Gradle 配置
├── gradle/
│   ├── libs.versions.toml      # 第三方依赖版本声明
│   └── wrapper/                # Gradle wrapper 脚本
├── gradle.properties           # Gradle 构建属性
├── gradlew / gradlew.bat       # Wrapper 执行脚本
└── settings.gradle.kts         # 多模块项目设置
```

### 层级说明

- **根目录**：包含构建脚本与 Gradle 配置，是整体项目的起点。
- **app/**：Android 应用的主体模块，`src/main` 目录存放主要代码与资源；`androidTest` 和 `test` 分别用于仪表化测试和单元测试。
- **java/com/example/cryptotrader/**：Kotlin 源码顶层包，按 `data`、`di`、`domain`、`ui` 等子目录划分。
  - **data/**：负责数据存取，包含本地数据库、网络仓库以及相关模型。
  - **di/**：定义 Hilt 提供的依赖注入模块。
  - **domain/**：业务用例层，封装具体操作逻辑。
  - **ui/**：Compose 界面和 ViewModel，`screens/` 子目录包含各个功能界面。
- **res/**：静态资源，如图片、主题配置及多语言文本。
- **gradle/**：存放版本管理文件和 Gradle wrapper，为构建系统提供配置。

## 主要模块 `app/src/main`

```
app/src/main
├── AndroidManifest.xml
├── java
│   └── com/example/cryptotrader
│       ├── MainActivity.kt
│       ├── TradingApplication.kt
│       ├── data/
│       │   ├── AppDatabase.kt
│       │   ├── FakeTicker.kt
│       │   ├── FavoriteDao.kt
│       │   ├── FavoritePair.kt
│       │   ├── FavoritesRepository.kt
│       │   ├── MarketModels.kt
│       │   ├── Order.kt
│       │   ├── OrderRepository.kt
│       │   ├── TickerRepository.kt
│       │   ├── local/
│       │   └── remote/
│       ├── di
│       ├── domain
│       └── ui
└── res
```

### UI 层示例

```
ui/screens
├── DetailScreen.kt
├── FavoritesEditScreen.kt
├── FavoritesListScreen.kt
├── FavoritesScreen.kt
├── MainScreen.kt
├── OrderHistoryScreen.kt
├── OrderScreen.kt
├── detail/
│   └── SpotDetailScreen.kt
└── trade/
    ├── TradeScreen.kt
    ├── TradeUiState.kt
    └── TradeViewModel.kt
```

### 资源文件

```
res
├── drawable/
├── mipmap-anydpi/
├── mipmap-hdpi/
├── mipmap-mdpi/
├── mipmap-xhdpi/
├── mipmap-xxhdpi/
├── mipmap-xxxhdpi/
├── values/
└── xml/
```

通过以上结构图可以更直观地了解项目的目录划分及各层级职责，便于后续开发和维护。

