# Android Hilt 依赖注入完整示例

本示例涵盖了Android Hilt依赖注入框架的主要功能和使用场景，包含详细的注释和日志输出。

## 📁 项目结构

```
app/src/main/java/com/gdet/testapp/hilt/
├── data/                          # 数据层
│   ├── api/                       # API服务层
│   │   └── ApiService.kt          # API接口和实现类
│   ├── local/                     # 本地数据层
│   │   └── DatabaseService.kt     # 数据库服务
│   └── models/                    # 数据模型
│       └── User.kt                # 用户和偏好设置模型
├── di/                            # 依赖注入配置
│   ├── modules/                   # Hilt模块
│   │   ├── NetworkModule.kt       # 网络相关依赖
│   │   ├── DatabaseModule.kt      # 数据库相关依赖
│   │   ├── CoroutineModule.kt     # 协程调度器
│   │   └── RepositoryModule.kt    # Repository层依赖
│   └── qualifiers/                # 限定符注解
│       └── Qualifiers.kt          # 自定义Qualifier注解
├── domain/                        # 业务逻辑层
│   └── repository/                # Repository层
│       └── UserRepository.kt      # 用户数据仓库
└── presentation/                  # 表现层
    ├── viewmodel/                 # ViewModel层
    │   └── UserViewModel.kt       # 用户ViewModel
    ├── HiltDemoActivity.kt        # 演示Activity
    └── HiltDemoFragment.kt        # 演示Fragment
```

## 🎯 学习重点

### 1. 基础配置

#### 1.1 Application类配置
```kotlin
@HiltAndroidApp
class AndroidTestApplication : Application() {
    // @HiltAndroidApp 注解是Hilt的入口点
    // 触发Hilt的代码生成，创建应用级依赖容器
}
```

#### 1.2 Gradle配置
```gradle
// 项目级build.gradle
plugins {
    id 'com.google.dagger.hilt.android' version '2.48' apply false
}

// 模块级build.gradle
plugins {
    id 'com.google.dagger.hilt.android'
}

dependencies {
    implementation 'com.google.dagger:hilt-android:2.48'
    kapt 'com.google.dagger:hilt-compiler:2.48'
    implementation 'androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03'
    kapt 'androidx.hilt:hilt-compiler:1.1.0'
}
```

### 2. 依赖注入类型

#### 2.1 构造函数注入 (Constructor Injection) - 推荐
```kotlin
@Singleton
class RealApiService @Inject constructor() : ApiService {
    // Hilt会自动创建实例并注入到需要的地方
}
```

#### 2.2 字段注入 (Field Injection)
```kotlin
@AndroidEntryPoint
class HiltDemoActivity : AppCompatActivity() {
    @Inject
    lateinit var appVersion: String  // 必须在onCreate()后使用
}
```

#### 2.3 方法注入 (Method Injection)
```kotlin
// 虽然本示例未使用，但语法如下：
@Inject
fun injectDependencies(apiService: ApiService) {
    // 方法会在对象创建后自动调用
}
```

### 3. Hilt模块 (@Module)

#### 3.1 @Provides - 提供具体实例
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }
}
```

#### 3.2 @Binds - 绑定接口实现（性能更好）
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {
    @Binds
    @RealApi
    abstract fun bindRealApiService(impl: RealApiService): ApiService
}
```

### 4. 作用域 (Scopes)

- `@Singleton`: 应用级单例，整个应用生命周期
- `@ActivityScoped`: Activity级作用域
- `@FragmentScoped`: Fragment级作用域
- `@ViewModelScoped`: ViewModel级作用域

### 5. 限定符 (Qualifiers)

用于区分同一类型的不同实现：

```kotlin
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class RealApi

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MockApi

// 使用
@Inject
@RealApi
lateinit var realApiService: ApiService

@Inject
@MockApi  
lateinit var mockApiService: ApiService
```

### 6. Android组件注入

#### 6.1 Activity注入
```kotlin
@AndroidEntryPoint
class HiltDemoActivity : AppCompatActivity() {
    private val viewModel: UserViewModel by viewModels()
    
    @Inject
    lateinit var appVersion: String
}
```

#### 6.2 Fragment注入
```kotlin
@AndroidEntryPoint
class HiltDemoFragment : Fragment() {
    // Fragment自己的ViewModel
    private val fragmentViewModel: UserViewModel by viewModels()
    
    // 与Activity共享的ViewModel
    private val sharedViewModel: UserViewModel by activityViewModels()
}
```

#### 6.3 ViewModel注入
```kotlin
@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository,
    @MainDispatcher private val dispatcher: CoroutineDispatcher
) : ViewModel()
```

### 7. Context注入

```kotlin
class LocalDatabaseService @Inject constructor(
    @ApplicationContext private val context: Context
) : DatabaseService
```

可用的Context Qualifier:
- `@ApplicationContext`: 应用程序Context
- `@ActivityContext`: Activity Context（在Activity及其Fragment中）

## 🔧 运行和测试

### 启动应用
1. 编译运行应用
2. 在主界面点击"Hilt依赖注入示例"
3. 观察Logcat输出，搜索"Hilt"标签

### 主要功能测试
1. **加载用户列表**: 点击"加载用户"按钮
2. **选择用户**: 点击"选择用户1"按钮  
3. **保存偏好设置**: 在Fragment中点击"保存用户偏好设置"

### 关键日志观察点

#### 应用启动时
```
HiltApp: Hilt应用程序启动 - Application onCreate()
HiltApp: Hilt依赖注入容器已准备就绪
```

#### 依赖创建时
```
HiltNetworkModule: 创建OkHttpClient实例 - @Provides方法
HiltCoroutineModule: 提供IO调度器 - Dispatchers.IO
HiltRealApiService: RealApiService 被创建 - Constructor Injection
HiltDatabaseService: LocalDatabaseService 被创建，Context: Application
HiltUserRepository: UserRepositoryImpl 被创建
```

#### ViewModel和Activity创建时
```
HiltUserViewModel: UserViewModel 被创建
HiltDemoActivity: Hilt注入的ViewModel: UserViewModel
HiltDemoActivity: Hilt注入的应用版本: 1.0.0
```

#### 数据操作时
```
HiltUserRepository: 开始获取用户列表 - 使用IO调度器
HiltRealApiService: 获取所有用户列表，共3个用户
HiltUserViewModel: 用户列表加载成功，共3个用户
```

## 🎓 学习要点总结

### 1. 核心概念
- **@HiltAndroidApp**: 应用入口点，必须在Application类上使用
- **@AndroidEntryPoint**: 标记需要依赖注入的Android组件
- **@Inject**: 标记需要注入的依赖或构造函数
- **@Module**: 定义如何提供依赖
- **@InstallIn**: 指定模块的组件和作用域

### 2. 最佳实践
- 优先使用构造函数注入
- 使用@Binds代替@Provides绑定接口（性能更好）
- 合理使用作用域避免内存泄漏
- 使用Qualifier区分同类型不同实现
- 在Repository层统一管理数据源

### 3. 架构优势
- **解耦**: 降低组件间的耦合度
- **测试**: 方便进行单元测试和集成测试
- **维护**: 依赖关系清晰，便于维护
- **扩展**: 容易添加新的实现或替换现有实现

### 4. 与其他框架对比
- **相比Dagger2**: 更简单的配置，Android组件友好
- **相比手动DI**: 自动生成代码，减少样板代码
- **相比Service Locator**: 编译时检查，更安全

## 🐛 常见问题

1. **编译错误**: 确保kapt插件正确配置
2. **注入失败**: 检查@AndroidEntryPoint注解是否添加
3. **循环依赖**: 重新设计依赖关系或使用Provider
4. **作用域错误**: 确保依赖的作用域匹配或更大

## 📚 扩展学习

1. **测试**: 学习如何使用@HiltAndroidTest进行测试
2. **自定义组件**: 创建自定义的Hilt组件
3. **多模块**: 在多模块项目中使用Hilt
4. **性能优化**: 了解Hilt的编译时优化

---

通过本示例，你应该能够掌握Hilt的核心概念和实际应用。建议按照日志输出逐步理解依赖注入的过程和时机。