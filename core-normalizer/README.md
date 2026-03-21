# core-normalizer 字段标准化框架

基于元注解驱动的字段标准化框架，通过注解声明字段的标准化规则，调用 `normalizeFields()` 统一执行。支持多种数据类型，可自由扩展。

## 架构

```
@NormalizeMarker(handler = XxxHandler::class)   ← 元注解，标记在具体标准化注解上
    ↓
@NormalizeString / @NormalizeList / 自定义注解   ← 具体注解，按类型分离参数
    ↓
FieldNormalizer<A : Annotation>                  ← 处理器接口，泛型绑定注解类型
    ↓
NormalizeExtensions.normalizeFields()            ← 统一入口，反射扫描所有带 @NormalizeMarker 的注解
```

## 内置注解一览

| 注解 | 适用类型 | 说明 |
|------|---------|------|
| `@NormalizeString` | `String` / `String?` | 字符串标准化，支持多个 action 按顺序执行 |
| `@NormalizeList` | `List` / `List?` | 列表标准化，支持去重和排序 |

### StringNormalizeAction

| Action | 说明 |
|--------|------|
| `TRIM` | 去除前后空白 |
| `LOWERCASE` | 转为小写 |
| `UPPERCASE` | 转为大写 |

### ListNormalizeAction

| Action | 说明 |
|--------|------|
| `DISTINCT` | 去重 |
| `SORT` | 排序（按 `toString()` 字典序） |

## 使用示例

### 基本使用

```kotlin
import nxcloud.foundation.core.normalizer.NormalizeExtensions.normalizeFields

data class MerchantDto(
    @field:NormalizeString(StringNormalizeAction.TRIM, StringNormalizeAction.LOWERCASE)
    var code: String? = null,

    @field:NormalizeString(StringNormalizeAction.TRIM)
    var name: String? = null,

    @field:NormalizeList(ListNormalizeAction.DISTINCT, ListNormalizeAction.SORT)
    var tags: List<String>? = null,
)

val dto = MerchantDto(
    code = "  ABC-001  ",
    name = "  Test Merchant  ",
    tags = listOf("c", "a", "b", "a"),
).normalizeFields()

// dto.code = "abc-001"
// dto.name = "Test Merchant"
// dto.tags = ["a", "b", "c"]
```

### 组合多个 Action

Action 按声明顺序依次执行：

```kotlin
data class UserDto(
    // 先 trim，再转小写
    @field:NormalizeString(StringNormalizeAction.TRIM, StringNormalizeAction.LOWERCASE)
    var code: String? = null,
)

val dto = UserDto(code = "  ABC-001  ").normalizeFields()
// dto.code = "abc-001"
```

## 自定义扩展

只需三步：

### 1. 实现 `FieldNormalizer` 接口

```kotlin
class NormalizeBigDecimalHandler : FieldNormalizer<NormalizeBigDecimal> {
    override fun normalize(annotation: NormalizeBigDecimal, value: Any): Any? {
        if (value !is BigDecimal) return value
        return value.setScale(annotation.scale, RoundingMode.HALF_UP)
    }
}
```

### 2. 定义注解并标注 `@NormalizeMarker`

```kotlin
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@NormalizeMarker(handler = NormalizeBigDecimalHandler::class)
annotation class NormalizeBigDecimal(
    val scale: Int = 2,
)
```

### 3. 使用

```kotlin
data class PriceDto(
    @field:NormalizeBigDecimal(scale = 4)
    var amount: BigDecimal? = null,
)

val dto = PriceDto(amount = BigDecimal("1.23456789")).normalizeFields()
// dto.amount = 1.2346
```

自定义注解可以与内置注解混合使用，同一个字段可以标注多个标准化注解，按声明顺序依次执行。
