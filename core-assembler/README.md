# core-assembler 装配器框架

基于注解驱动的实体关联自动装配框架，用于将关联实体的数据自动填充到包装类（Wrapper DTO）中，支持单个装配和批量装配（自动合并查询优化性能）。

## 注解一览

| 注解 | 标记位置 | 说明 |
|------|---------|------|
| `@AssemblableWrapper` | 包装类 | 标记一个类为可装配的包装类 |
| `@AssemblyEntity` | 包装类字段 | 标记包装类中持有的源实体字段（组合模式） |
| `@AssemblyRefSource` | 实体字段 | 标记实体中的外键字段，声明其指向的关联实体类型 |
| `@AssemblyField` | 包装类字段 | 正向多对一：通过源实体的外键加载关联实体 |
| `@AssemblyListField` | 包装类字段 | 反向一对多：通过子实体的外键反向查询列表 |
| `@AssemblyBatchField` | 包装类字段 | 正向批量：通过源实体的 ID 列表批量加载关联实体列表 |
| `@AssemblyReverseField` | 包装类字段 | 反向一对一：通过子实体的外键反向查询单个实体或提取其属性 |

## 两种包装模式

### 组合模式

包装类持有一个源实体字段，用 `@AssemblyEntity` 标记：

```kotlin
@AssemblableWrapper
data class OrderWrapperDto(
    @field:AssemblyEntity
    val order: Order,

    @field:AssemblyField
    var customer: Customer? = null,
)
```

### 继承模式

包装类直接继承源实体类，无需 `@AssemblyEntity`：

```kotlin
@AssemblableWrapper
class OrderWrapperDto(
    id: Long = 0L,
    orderNo: String = "",
    customerId: Long = 0L,

    @field:AssemblyField
    var customer: Customer? = null,
) : Order(id, orderNo, customerId)
```

---

## 场景 1：多对一关联（@AssemblyField）

**场景**：订单中有 `customerId` 外键，需要加载对应的 `Customer` 对象。

### 实体定义

```kotlin
data class Customer(
    val id: Long,
    val name: String,
)

open class Order(
    open var id: Long = 0L,
    open var orderNo: String = "",

    @AssemblyRefSource(source = Customer::class, sourceField = "id")
    open var customerId: Long = 0L,
)
```

### 包装类

```kotlin
@AssemblableWrapper
class OrderWrapperDto(
    id: Long = 0L,
    orderNo: String = "",
    customerId: Long = 0L,

    // 自动根据类型匹配 @AssemblyRefSource 字段，加载整个 Customer 对象
    @field:AssemblyField
    var customer: Customer? = null,

    // 指定从 customerId 关联加载 Customer，只提取 name 属性
    @field:AssemblyField(entityField = "customerId", targetField = "name")
    var customerName: String? = null,
) : Order(id, orderNo, customerId)
```

### 注解参数说明

| 参数 | 说明 |
|------|------|
| `entityField` | 源实体中标记了 `@AssemblyRefSource` 的外键字段名，未指定则按类型自动匹配 |
| `targetField` | 从加载的关联实体中提取的属性名，未指定则填充整个对象 |

---

## 场景 2：一对多反向关联（@AssemblyListField）

**场景**：父实体需要加载其所有子实体列表，子实体中有外键指向父实体。

### 实体定义

```kotlin
open class Department(
    open var id: Long = 0L,
    open var name: String = "",
)

data class Employee(
    val id: Long,
    val name: String,

    @AssemblyRefSource(source = Department::class)
    val departmentId: Long,
)
```

### 包装类

```kotlin
@AssemblableWrapper
class DepartmentWrapperDto(
    id: Long = 0L,
    name: String = "",

    @field:AssemblyListField
    var employees: List<Employee>? = null,
) : Department(id, name)
```

框架会自动从 `List<Employee>` 的泛型参数获取目标类型 `Employee`，在其中查找 `@AssemblyRefSource(source = Department::class)` 的字段 `departmentId`，然后通过主实体的 `id` 反向查询。

### 嵌套装配

列表元素类型也可以是 `@AssemblableWrapper`，框架会自动递归装配：

```kotlin
@AssemblableWrapper
class EmployeeWrapperDto(
    id: Long = 0L,
    name: String = "",
    departmentId: Long = 0L,

    @field:AssemblyField
    var role: Role? = null,
) : Employee(id, name, departmentId)

@AssemblableWrapper
class DepartmentWrapperDto(
    id: Long = 0L,
    name: String = "",

    @field:AssemblyListField
    var employees: List<EmployeeWrapperDto>? = null,  // 嵌套 Wrapper，会递归装配
) : Department(id, name)
```

---

## 场景 3：正向批量加载（@AssemblyBatchField）

**场景**：实体中有一个 ID 列表字段，需要批量加载对应的关联实体列表。

### 实体定义

```kotlin
data class Tag(
    val id: Long,
    val name: String,
)

open class Article(
    open var id: Long = 0L,
    open var title: String = "",

    @AssemblyRefSource(source = Tag::class, sourceField = "id")
    open var tagIdList: List<Long> = emptyList(),
)
```

### 包装类

```kotlin
@AssemblableWrapper
class ArticleWrapperDto(
    id: Long = 0L,
    title: String = "",
    tagIdList: List<Long> = emptyList(),

    @field:AssemblyBatchField(entityField = "tagIdList")
    var tags: List<Tag>? = null,
) : Article(id, title, tagIdList)
```

### 注解参数说明

| 参数 | 说明 |
|------|------|
| `entityField` | 源实体中存储 ID 列表的字段名（需标记 `@AssemblyRefSource`），未指定则按类型自动匹配 |

---

## 场景 4：一对一反向关联（@AssemblyReverseField）

**场景**：主实体需要加载一个反向关联的子实体（子实体有外键指向主实体），并提取其中的特定属性。典型场景如商户与商户账户的一对一关系。

### 实体定义

```kotlin
open class Merchant(
    open var id: Long = 0L,
    open var name: String = "",
)

data class MerchantAccount(
    val id: Long,
    val balance: BigDecimal,
    val totalRecharge: BigDecimal,
    val totalConsume: BigDecimal,

    @AssemblyRefSource(source = Merchant::class)
    val merchantId: Long,
)
```

### 包装类

```kotlin
@AssemblableWrapper
class MerchantWrapperDto(
    id: Long = 0L,
    name: String = "",

    // 提取 MerchantAccount 的 balance 属性
    @field:AssemblyReverseField(target = MerchantAccount::class, sourceProperty = "balance")
    var balance: BigDecimal = BigDecimal.ZERO,

    // 提取 MerchantAccount 的 totalRecharge 属性
    @field:AssemblyReverseField(target = MerchantAccount::class, sourceProperty = "totalRecharge")
    var totalRecharge: BigDecimal = BigDecimal.ZERO,

    // 提取 MerchantAccount 的 totalConsume 属性
    @field:AssemblyReverseField(target = MerchantAccount::class, sourceProperty = "totalConsume")
    var totalConsume: BigDecimal = BigDecimal.ZERO,

    // 不指定 sourceProperty，填充整个 MerchantAccount 对象
    @field:AssemblyReverseField(target = MerchantAccount::class)
    var account: MerchantAccount? = null,
) : Merchant(id, name)
```

多个 `@AssemblyReverseField` 指向同一个 `target` 类时，批量装配只会发起一次查询。

### 注解参数说明

| 参数 | 说明 |
|------|------|
| `target` | 反向引用的目标实体类型（包含 `@AssemblyRefSource` 指向主实体的类） |
| `targetField` | 目标实体中的外键字段名，未指定则自动查找指向主实体的唯一 `@AssemblyRefSource` 字段 |
| `sourceProperty` | 从目标实体中提取的属性名，未指定则填充整个对象 |

---

## 装配器使用

### 单个装配

```kotlin
val assembler = AssemblableWrapperAssembler()
val result = assembler.assemble(wrapper, dataProvider)
```

`dataProvider` 需实现 `RefSourceDataProvider` 接口。如果包装类中使用了 `@AssemblyListField` 或 `@AssemblyReverseField`，还需同时实现 `BatchListRefSourceDataProvider`。

### 批量装配

```kotlin
val assembler = AssemblableWrapperAssembler()
val results = assembler.assembleBatch(wrappers, batchDataProvider)
```

`batchDataProvider` 需实现 `BatchRefSourceDataProvider` 接口。如果使用了 `@AssemblyListField` 或 `@AssemblyReverseField`，还需同时实现 `BatchListRefSourceDataProvider`。

批量装配会自动合并相同类型的查询请求，避免 N+1 问题。

### 数据提供者接口

```kotlin
// 单个加载（用于 assemble）
interface RefSourceDataProvider {
    fun <T : Any> load(source: KClass<T>, metadata: RefSourceFieldMetadata, sourceFieldValue: Any?): T?
}

// 批量加载（用于 assembleBatch 的 @AssemblyField 和 @AssemblyBatchField）
interface BatchRefSourceDataProvider {
    fun <T : Any> loadBatch(source: KClass<T>, metadata: RefSourceFieldMetadata, sourceFieldValues: Set<Any>): Map<Any, T>
}

// 列表批量加载（用于 @AssemblyListField 和 @AssemblyReverseField）
interface BatchListRefSourceDataProvider {
    fun <T : Any> loadListBatch(target: KClass<T>, targetFieldName: String, sourceFieldValues: Set<Any>): Map<Any, List<T>>
    fun <S : Any, T : Any> mapList(source: List<S>, targetClass: KClass<T>): List<T>
}
```

## @AssemblyRefSource 注解

`@AssemblyRefSource` 是整个框架的基础，用于在实体字段上声明外键关联关系：

```kotlin
@AssemblyRefSource(source = Customer::class, sourceField = "id")
var customerId: Long = 0L
```

| 参数 | 说明 |
|------|------|
| `source` | 关联的目标实体类型 |
| `sourceField` | 目标实体中用于匹配的字段名（如 `"id"`），未指定则默认按主键匹配 |
