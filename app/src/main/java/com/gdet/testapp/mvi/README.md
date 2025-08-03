# Android MVI 架构示例

本目录包含了两个完整的Android MVI（Model-View-Intent）架构示例，展示了从简单到复杂的MVI实现。

## 目录结构

```
mvi/
├── simple/          # 简单的MVI计数器示例
│   ├── CounterIntent.kt
│   ├── CounterAction.kt
│   ├── CounterState.kt
│   ├── CounterReducer.kt
│   ├── CounterViewModel.kt
│   ├── CounterActivity.kt          # 传统View版本
│   └── CounterComposeActivity.kt   # Compose版本
└── complete/        # 完整的MVI用户列表示例
    ├── data/        # 数据层
    │   ├── User.kt
    │   ├── UserApiService.kt
    │   └── UserRepository.kt
    ├── UserListIntent.kt
    ├── UserListAction.kt
    ├── UserListState.kt
    ├── UserListReducer.kt
    ├── UserListViewModel.kt
    ├── UserListActivity.kt
    └── UserListAdapter.kt
```

## 第一部分：简单的MVI计数器示例

### 功能特性
- 基础的计数器操作（增加、减少、重置）
- 自定义数值设置
- 操作历史记录
- 错误处理和边界检查
- 同时提供传统View和Compose两个版本

### MVI组件说明

#### 1. CounterIntent（用户意图）
```kotlin
sealed class CounterIntent {
    object Increment : CounterIntent()
    object Decrement : CounterIntent()
    object Reset : CounterIntent()
    data class SetValue(val value: Int) : CounterIntent()
}
```
- 定义用户可能的操作意图
- 使用sealed class确保类型安全

#### 2. CounterAction（动作）
```kotlin
sealed class CounterAction {
    object IncrementCounter : CounterAction()
    object DecrementCounter : CounterAction()
    object ResetCounter : CounterAction()
    data class SetCounterValue(val value: Int) : CounterAction()
}
```
- 描述具体要执行的动作
- 在简单场景中与Intent一一对应

#### 3. CounterState（状态）
```kotlin
data class CounterState(
    val count: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isOperationEnabled: Boolean = true,
    val history: List<String> = emptyList()
)
```
- 包含UI渲染所需的完整状态
- 不可变数据类
- 提供便利方法简化UI逻辑

#### 4. CounterReducer（状态缩减器）
```kotlin
object CounterReducer {
    fun reduce(currentState: CounterState, action: CounterAction): CounterState {
        // 纯函数，根据当前状态和动作计算新状态
    }
}
```
- 纯函数，无副作用
- 集中处理所有状态变化逻辑

#### 5. CounterViewModel
- 接收Intent并转换为Action
- 使用Reducer更新状态
- 向UI暴露状态流

## 第二部分：完整的MVI用户列表示例

### 功能特性
- 用户列表展示和管理
- 搜索和过滤功能
- 分页加载和下拉刷新
- 多选模式和批量操作
- CRUD操作（创建、读取、更新、删除）
- 网络请求和错误处理
- 本地缓存管理

### 架构层次

#### 数据层
- **User.kt**: 用户数据模型，包含过滤和排序逻辑
- **UserApiService.kt**: 网络API接口，使用模拟数据演示
- **UserRepository.kt**: 数据仓库，统一数据访问，管理缓存

#### MVI层
- **UserListIntent.kt**: 复杂的用户意图定义（20+种意图）
- **UserListAction.kt**: 详细的动作定义
- **UserListState.kt**: 复杂状态管理，包含多种UI状态
- **UserListReducer.kt**: 状态缩减器，处理复杂状态变化
- **UserListViewModel.kt**: 协调数据操作和状态管理

#### UI层
- **UserListActivity.kt**: 主界面，展示MVI在复杂UI中的应用
- **UserListAdapter.kt**: RecyclerView适配器，支持多选和操作

### 高级特性展示

1. **异步操作处理**
   - 网络请求状态管理
   - 加载、刷新、错误状态

2. **复杂状态管理**
   - 搜索状态
   - 过滤和排序状态
   - 多选状态
   - 操作状态

3. **数据流管理**
   - Repository模式
   - 响应式数据流
   - 缓存策略

4. **用户体验优化**
   - 分页加载
   - 下拉刷新
   - 错误重试
   - 操作反馈

## MVI架构优势

1. **单向数据流**: Intent → Action → State → View
2. **状态可预测**: 相同的State总是产生相同的UI
3. **易于测试**: 纯函数Reducer，状态变化可预测
4. **时间旅行调试**: 可以记录和回放状态变化
5. **并发安全**: 不可变状态避免并发问题

## 使用的技术栈

- **Kotlin**: 主要编程语言
- **Coroutines**: 异步编程
- **Flow**: 响应式数据流
- **ViewModel**: 生命周期感知的数据管理
- **ViewBinding**: 类型安全的视图绑定
- **RecyclerView**: 列表展示
- **Material Design**: UI设计规范

## 运行示例

1. 构建项目：`./gradlew assembleDebug`
2. 运行简单示例：启动 `CounterActivity`
3. 运行完整示例：启动 `UserListActivity`

## 学习建议

1. 先理解简单示例中的MVI基本概念
2. 观察状态变化和数据流向
3. 学习复杂示例中的高级特性
4. 尝试添加新功能来加深理解

这些示例展示了MVI架构在Android开发中的实际应用，从基础概念到复杂场景，帮助开发者理解和掌握MVI架构模式。
