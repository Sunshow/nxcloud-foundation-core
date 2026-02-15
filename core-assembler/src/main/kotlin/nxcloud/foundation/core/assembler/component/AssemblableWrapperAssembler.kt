package nxcloud.foundation.core.assembler.component

import nxcloud.foundation.core.assembler.annotation.*
import org.apache.commons.beanutils.PropertyUtils
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.util.ReflectionUtils
import java.lang.reflect.ParameterizedType
import java.lang.reflect.WildcardType
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.kotlinProperty

/**
 * 可组装包装类的装配器
 *
 * 根据注解自动填充包装类中的关联字段，支持三种关联模式：
 *
 * ## 1. 多对一关联（@AssemblyField）
 * 从主实体的外键字段加载关联的单个实体
 *
 * ## 2. 一对多反向关联（@AssemblyListField）
 * 反向查询子实体列表，子实体中需标记指向主实体的外键
 *
 * ## 3. 正向批量加载（@AssemblyBatchField）
 * 从主实体的 ID 列表字段批量加载目标实体列表
 *
 * ## 4. 嵌套装配
 * 列表元素类型也可以是 @AssemblableWrapper，会自动递归装配
 */
class AssemblableWrapperAssembler {

    // ==================== 元数据定义 ====================

    data class WrapperMetadata(
        val annotation: AssemblableWrapper,
        val entity: EntityMetadata,
        val fields: Map<String, FieldMetadata>,
        val listFields: Map<String, ListFieldMetadata> = emptyMap(),
        val batchFields: Map<String, BatchFieldMetadata> = emptyMap(),
    )

    data class EntityMetadata(
        val annotation: AssemblyEntity?,
        val property: KProperty1<*, *>?,
        val entityClass: KClass<*>,
        val isInheritanceMode: Boolean = false,
    )

    data class FieldMetadata(
        val annotation: AssemblyField,
        val property: KProperty1<*, *>,
        val fieldClass: KClass<*>,
        val sourceField: RefSourceFieldMetadata,
    )

    data class ListFieldMetadata(
        val property: KProperty1<*, *>,
        val elementClass: KClass<*>,
        val refSourceField: RefSourceFieldMetadata,
        val isNestedWrapper: Boolean = false,
        val sourceClass: KClass<*>? = null,
    )

    data class BatchFieldMetadata(
        val annotation: AssemblyBatchField,
        val property: KProperty1<*, *>,
        val elementClass: KClass<*>,
        val sourceField: RefSourceFieldMetadata,
    )

    data class RefSourceMetadata(
        val fields: Map<String, RefSourceFieldMetadata>,
    )

    data class RefSourceFieldMetadata(
        val annotation: AssemblyRefSource,
        val property: KProperty1<*, *>,
        val sourceClass: KClass<*>,
        val sourceFieldName: String,
    )

    private val metadataCache = ConcurrentHashMap<KClass<*>, WrapperMetadata>()

    private val refSourceCache = ConcurrentHashMap<KClass<*>, RefSourceMetadata>()

    // ==================== 数据提供者接口 ====================

    interface RefSourceDataProvider {
        fun <T : Any> load(source: KClass<T>, metadata: RefSourceFieldMetadata, sourceFieldValue: Any?): T?
    }

    interface BatchRefSourceDataProvider {
        fun <T : Any> loadBatch(
            source: KClass<T>,
            metadata: RefSourceFieldMetadata,
            sourceFieldValues: Set<Any>
        ): Map<Any, T>
    }

    interface BatchListRefSourceDataProvider {
        fun <T : Any> loadListBatch(
            target: KClass<T>,
            targetFieldName: String,
            sourceFieldValues: Set<Any>
        ): Map<Any, List<T>>

        fun <S : Any, T : Any> mapList(source: List<S>, targetClass: KClass<T>): List<T>
    }

    // ==================== 批量加载请求/结果 ====================

    data class RefSourceKey(
        val sourceClass: KClass<*>,
        val sourceFieldName: String,
        val metadata: RefSourceFieldMetadata
    )

    data class ListRefSourceKey(
        val targetClass: KClass<*>,
        val targetFieldName: String,
    )

    data class BatchAssemblyRequest(
        val loadRequests: Map<RefSourceKey, MutableSet<Any>>
    )

    data class BatchLoadResult(
        val loadedData: Map<RefSourceKey, Map<Any, Any>>
    )

    data class BatchListAssemblyRequest(
        val loadRequests: Map<ListRefSourceKey, MutableSet<Any>>
    )

    data class BatchListLoadResult(
        val loadedData: Map<ListRefSourceKey, Map<Any, List<Any>>>
    )

    // ==================== 元数据解析 ====================

    private fun resolveWrapperMetadata(wrapperClass: KClass<*>): WrapperMetadata {
        return metadataCache.computeIfAbsent(wrapperClass) {
            val wrapperAnnotation = wrapperClass.findAnnotation<AssemblableWrapper>()
                ?: throw IllegalArgumentException("类 ${wrapperClass.simpleName} 未标记 @AssemblableWrapper 注解")

            val entityProperty = wrapperClass.memberProperties
                .firstOrNull { property ->
                    property.javaField?.getAnnotation(AssemblyEntity::class.java) != null
                }

            val entityMetadata = if (entityProperty != null) {
                val entityAnnotation = entityProperty.javaField?.getAnnotation(AssemblyEntity::class.java)
                    ?: throw IllegalArgumentException("类 ${wrapperClass.simpleName} 中未找到 @AssemblyEntity 标记的字段")
                val entityClass = entityProperty.returnType.classifier as KClass<*>
                EntityMetadata(
                    annotation = entityAnnotation,
                    property = entityProperty,
                    entityClass = entityClass,
                    isInheritanceMode = false
                )
            } else {
                val superclass = wrapperClass.superclasses
                    .firstOrNull { it != Any::class }
                    ?: throw IllegalArgumentException(
                        "类 ${wrapperClass.simpleName} 未标记 @AssemblyEntity 字段，" +
                                "也没有继承 Entity 类，无法确定源实体"
                    )
                EntityMetadata(
                    annotation = null,
                    property = null,
                    entityClass = superclass,
                    isInheritanceMode = true
                )
            }

            val refSourceMetadata = resolveRefSourceMetadata(entityMetadata.entityClass)

            // 解析 @AssemblyField 注解
            val fieldMetadataMap = wrapperClass.memberProperties
                .mapNotNull { property ->
                    val fieldAnnotation = property.javaField?.getAnnotation(AssemblyField::class.java)
                    if (fieldAnnotation != null) {
                        val fieldClass = property.returnType.classifier as KClass<*>
                        val refSourceFieldMetadata = if (fieldAnnotation.entityField.isNotEmpty()) {
                            refSourceMetadata.fields[fieldAnnotation.entityField]
                                ?: throw IllegalArgumentException("在 ${entityMetadata.entityClass.simpleName} 中未找到字段 '${fieldAnnotation.entityField}' 的 @AssemblyRefSource 注解")
                        } else {
                            refSourceMetadata.fields
                                .filter {
                                    it.value.sourceClass == fieldClass
                                }
                                .takeIf {
                                    it.size == 1
                                }
                                ?.values
                                ?.firstOrNull()
                                ?: throw IllegalArgumentException("在 ${entityMetadata.entityClass.simpleName} 中未找到指向 ${fieldClass.simpleName} 的唯一 @AssemblyRefSource 字段")
                        }

                        property.name to FieldMetadata(
                            annotation = fieldAnnotation,
                            property = property,
                            fieldClass = fieldClass,
                            sourceField = refSourceFieldMetadata,
                        )
                    } else {
                        null
                    }
                }
                .toMap()

            // 解析 @AssemblyListField 注解
            val listFieldMetadataMap = wrapperClass.memberProperties
                .mapNotNull { property ->
                    val listFieldAnnotation = property.javaField?.getAnnotation(AssemblyListField::class.java)
                    if (listFieldAnnotation != null) {
                        val genericType = property.javaField?.genericType
                        val elementClass = if (genericType is ParameterizedType) {
                            val typeArg = genericType.actualTypeArguments.firstOrNull()
                            when (typeArg) {
                                is Class<*> -> typeArg.kotlin
                                is WildcardType -> (typeArg.upperBounds.firstOrNull() as? Class<*>)?.kotlin
                                else -> null
                            }
                        } else {
                            null
                        } ?: throw IllegalArgumentException(
                            "无法从 ${property.name} 获取 List 元素类型"
                        )

                        val isNestedWrapper = elementClass.findAnnotation<AssemblableWrapper>() != null

                        val sourceClass = if (isNestedWrapper) {
                            elementClass.superclasses.firstOrNull { it != Any::class }
                        } else {
                            null
                        }

                        val refSourceEntityClass = sourceClass ?: elementClass
                        val elementRefSourceMetadata = resolveRefSourceMetadata(refSourceEntityClass)
                        val refSourceFieldMetadata = elementRefSourceMetadata.fields.values
                            .firstOrNull { it.sourceClass == entityMetadata.entityClass }
                            ?: throw IllegalArgumentException(
                                "在 ${refSourceEntityClass.simpleName} 中未找到指向 ${entityMetadata.entityClass.simpleName} 的 @AssemblyRefSource 字段"
                            )

                        property.name to ListFieldMetadata(
                            property = property,
                            elementClass = elementClass,
                            refSourceField = refSourceFieldMetadata,
                            isNestedWrapper = isNestedWrapper,
                            sourceClass = sourceClass,
                        )
                    } else {
                        null
                    }
                }
                .toMap()

            // 解析 @AssemblyBatchField 注解
            val batchFieldMetadataMap = wrapperClass.memberProperties
                .mapNotNull { property ->
                    val batchFieldAnnotation = property.javaField?.getAnnotation(AssemblyBatchField::class.java)
                    if (batchFieldAnnotation != null) {
                        val genericType = property.javaField?.genericType
                        val elementClass = if (genericType is ParameterizedType) {
                            val typeArg = genericType.actualTypeArguments.firstOrNull()
                            when (typeArg) {
                                is Class<*> -> typeArg.kotlin
                                is WildcardType -> (typeArg.upperBounds.firstOrNull() as? Class<*>)?.kotlin
                                else -> null
                            }
                        } else {
                            null
                        } ?: throw IllegalArgumentException(
                            "无法从 ${property.name} 获取 List 元素类型"
                        )

                        val refSourceFieldMetadata = if (batchFieldAnnotation.entityField.isNotEmpty()) {
                            refSourceMetadata.fields[batchFieldAnnotation.entityField]
                                ?: throw IllegalArgumentException(
                                    "在 ${entityMetadata.entityClass.simpleName} 中未找到字段 '${batchFieldAnnotation.entityField}' 的 @AssemblyRefSource 注解"
                                )
                        } else {
                            refSourceMetadata.fields
                                .filter { it.value.sourceClass == elementClass }
                                .takeIf { it.size == 1 }
                                ?.values
                                ?.firstOrNull()
                                ?: throw IllegalArgumentException(
                                    "在 ${entityMetadata.entityClass.simpleName} 中未找到指向 ${elementClass.simpleName} 的唯一 @AssemblyRefSource 字段"
                                )
                        }

                        property.name to BatchFieldMetadata(
                            annotation = batchFieldAnnotation,
                            property = property,
                            elementClass = elementClass,
                            sourceField = refSourceFieldMetadata,
                        )
                    } else {
                        null
                    }
                }
                .toMap()

            WrapperMetadata(
                annotation = wrapperAnnotation,
                entity = entityMetadata,
                fields = fieldMetadataMap,
                listFields = listFieldMetadataMap,
                batchFields = batchFieldMetadataMap,
            )
        }
    }

    private fun resolveRefSourceMetadata(entityClass: KClass<*>): RefSourceMetadata {
        return refSourceCache.computeIfAbsent(entityClass) {
            val fieldMetadataMap = mutableMapOf<String, RefSourceFieldMetadata>()

            ReflectionUtils.doWithFields(entityClass.java) { field ->
                val refSourceAnnotation = AnnotationUtils.findAnnotation(field, AssemblyRefSource::class.java)
                if (refSourceAnnotation != null && !fieldMetadataMap.containsKey(field.name)) {
                    val property = field.kotlinProperty as KProperty1<*, *>
                    fieldMetadataMap[field.name] = RefSourceFieldMetadata(
                        annotation = refSourceAnnotation,
                        property = property,
                        sourceClass = refSourceAnnotation.source,
                        sourceFieldName = refSourceAnnotation.sourceField,
                    )
                }
            }

            RefSourceMetadata(fields = fieldMetadataMap)
        }
    }

    // ==================== 公共 API ====================

    fun <T : Any> assemble(wrapper: T, dataProvider: RefSourceDataProvider): T {
        val wrapperClass = wrapper::class

        val wrapperMetadata = resolveWrapperMetadata(wrapperClass)

        val entityMetadata = wrapperMetadata.entity

        val sourceEntity: Any = if (entityMetadata.isInheritanceMode) {
            wrapper
        } else {
            PropertyUtils.getProperty(wrapper, entityMetadata.property!!.name)
                ?: throw IllegalArgumentException("源实体不能为空")
        }

        wrapperMetadata.fields
            .forEach { (_, fieldMetadata) ->
                val sourceFieldValue = PropertyUtils.getProperty(sourceEntity, fieldMetadata.sourceField.property.name)

                val refEntity = dataProvider
                    .load(
                        source = fieldMetadata.sourceField.sourceClass,
                        metadata = fieldMetadata.sourceField,
                        sourceFieldValue = sourceFieldValue,
                    )

                if (refEntity != null) {
                    val valueToSet = if (fieldMetadata.annotation.targetField.isNotEmpty()) {
                        PropertyUtils.getProperty(refEntity, fieldMetadata.annotation.targetField)
                    } else {
                        refEntity
                    }

                    PropertyUtils.setProperty(wrapper, fieldMetadata.property.name, valueToSet)
                }
            }

        // 处理 @AssemblyListField 字段
        if (dataProvider is BatchListRefSourceDataProvider && wrapperMetadata.listFields.isNotEmpty()) {
            wrapperMetadata.listFields.forEach { (_, listFieldMetadata) ->
                val sourceFieldName = listFieldMetadata.refSourceField.sourceFieldName.ifEmpty { "id" }
                val sourceFieldValue = PropertyUtils.getProperty(sourceEntity, sourceFieldName)

                if (sourceFieldValue != null) {
                    val queryTargetClass = listFieldMetadata.sourceClass ?: listFieldMetadata.elementClass

                    @Suppress("UNCHECKED_CAST")
                    val result = dataProvider.loadListBatch(
                        target = queryTargetClass as KClass<Any>,
                        targetFieldName = listFieldMetadata.refSourceField.property.name,
                        sourceFieldValues = setOf(sourceFieldValue)
                    )
                    result[sourceFieldValue]?.let { listData ->
                        val finalList =
                            if (listFieldMetadata.isNestedWrapper && dataProvider is BatchRefSourceDataProvider) {
                                @Suppress("UNCHECKED_CAST")
                                val wrapperList = dataProvider.mapList(
                                    listData,
                                    listFieldMetadata.elementClass as KClass<Any>
                                )
                                assembleBatch(wrapperList, dataProvider)
                            } else {
                                listData
                            }
                        PropertyUtils.setProperty(wrapper, listFieldMetadata.property.name, finalList)
                    }
                }
            }
        }

        return wrapper
    }

    fun <T : Any> assembleBatch(wrappers: List<T>, dataProvider: BatchRefSourceDataProvider): List<T> {
        if (wrappers.isEmpty()) return wrappers

        // 阶段1: 收集所有 @AssemblyField 和 @AssemblyBatchField 需要加载的外键值
        val request = collectLoadRequests(wrappers)

        // 阶段2: 批量加载所有关联实体
        val loadResult = batchLoad(request, dataProvider)

        // 阶段3: 将加载的数据分发填充到各个 Wrapper
        val result = distributeAndFill(wrappers, loadResult)

        // 阶段4: 处理 @AssemblyListField（一对多关联）
        if (dataProvider is BatchListRefSourceDataProvider) {
            val listRequest = collectListLoadRequests(result)
            val listLoadResult = batchListLoad(listRequest, dataProvider)
            distributeAndFillList(result, listLoadResult, dataProvider)
        }

        return result
    }

    // ==================== 多对一关联处理（@AssemblyField）====================

    private fun <T : Any> collectLoadRequests(wrappers: List<T>): BatchAssemblyRequest {
        val loadRequests = mutableMapOf<RefSourceKey, MutableSet<Any>>()

        wrappers.forEach { wrapper ->
            val wrapperClass = wrapper::class
            val wrapperMetadata = resolveWrapperMetadata(wrapperClass)
            val entityMetadata = wrapperMetadata.entity

            val sourceEntity: Any = if (entityMetadata.isInheritanceMode) {
                wrapper
            } else {
                PropertyUtils.getProperty(wrapper, entityMetadata.property!!.name)
                    ?: return@forEach
            }

            wrapperMetadata.fields.forEach { (_, fieldMetadata) ->
                val sourceFieldValue = PropertyUtils.getProperty(
                    sourceEntity,
                    fieldMetadata.sourceField.property.name
                )

                if (sourceFieldValue != null) {
                    val key = RefSourceKey(
                        sourceClass = fieldMetadata.sourceField.sourceClass,
                        sourceFieldName = fieldMetadata.sourceField.sourceFieldName,
                        metadata = fieldMetadata.sourceField
                    )
                    loadRequests
                        .getOrPut(key) { mutableSetOf() }
                        .add(sourceFieldValue)
                }
            }

            // 收集 @AssemblyBatchField 的 ID 列表
            wrapperMetadata.batchFields.forEach { (_, batchFieldMetadata) ->
                val sourceFieldValue = PropertyUtils.getProperty(
                    sourceEntity,
                    batchFieldMetadata.sourceField.property.name
                )

                if (sourceFieldValue is Collection<*>) {
                    val key = RefSourceKey(
                        sourceClass = batchFieldMetadata.sourceField.sourceClass,
                        sourceFieldName = batchFieldMetadata.sourceField.sourceFieldName,
                        metadata = batchFieldMetadata.sourceField
                    )
                    sourceFieldValue.filterNotNull().forEach { id ->
                        loadRequests
                            .getOrPut(key) { mutableSetOf() }
                            .add(id)
                    }
                }
            }
        }

        return BatchAssemblyRequest(loadRequests)
    }

    private fun batchLoad(
        request: BatchAssemblyRequest,
        dataProvider: BatchRefSourceDataProvider
    ): BatchLoadResult {
        val loadedData = mutableMapOf<RefSourceKey, Map<Any, Any>>()

        request.loadRequests.forEach { (key, values) ->
            if (values.isNotEmpty()) {
                @Suppress("UNCHECKED_CAST")
                val result = dataProvider.loadBatch(
                    source = key.sourceClass as KClass<Any>,
                    metadata = key.metadata,
                    sourceFieldValues = values
                )
                loadedData[key] = result
            }
        }

        return BatchLoadResult(loadedData)
    }

    private fun <T : Any> distributeAndFill(
        wrappers: List<T>,
        loadResult: BatchLoadResult
    ): List<T> {
        wrappers.forEach { wrapper ->
            val wrapperClass = wrapper::class
            val wrapperMetadata = resolveWrapperMetadata(wrapperClass)
            val entityMetadata = wrapperMetadata.entity

            val sourceEntity: Any = if (entityMetadata.isInheritanceMode) {
                wrapper
            } else {
                PropertyUtils.getProperty(wrapper, entityMetadata.property!!.name)
                    ?: return@forEach
            }

            // 处理 @AssemblyField（多对一）
            wrapperMetadata.fields.forEach { (_, fieldMetadata) ->
                val sourceFieldValue = PropertyUtils.getProperty(
                    sourceEntity,
                    fieldMetadata.sourceField.property.name
                )

                if (sourceFieldValue != null) {
                    val key = RefSourceKey(
                        sourceClass = fieldMetadata.sourceField.sourceClass,
                        sourceFieldName = fieldMetadata.sourceField.sourceFieldName,
                        metadata = fieldMetadata.sourceField
                    )

                    val refEntity = loadResult.loadedData[key]?.get(sourceFieldValue)

                    if (refEntity != null) {
                        val valueToSet = if (fieldMetadata.annotation.targetField.isNotEmpty()) {
                            PropertyUtils.getProperty(refEntity, fieldMetadata.annotation.targetField)
                        } else {
                            refEntity
                        }
                        PropertyUtils.setProperty(wrapper, fieldMetadata.property.name, valueToSet)
                    }
                }
            }

            // 处理 @AssemblyBatchField（正向批量加载）
            wrapperMetadata.batchFields.forEach { (_, batchFieldMetadata) ->
                val sourceFieldValue = PropertyUtils.getProperty(
                    sourceEntity,
                    batchFieldMetadata.sourceField.property.name
                )

                if (sourceFieldValue is Collection<*>) {
                    val key = RefSourceKey(
                        sourceClass = batchFieldMetadata.sourceField.sourceClass,
                        sourceFieldName = batchFieldMetadata.sourceField.sourceFieldName,
                        metadata = batchFieldMetadata.sourceField
                    )

                    val loadedDataMap = loadResult.loadedData[key]
                    if (loadedDataMap != null) {
                        val resultList = sourceFieldValue
                            .filterNotNull()
                            .mapNotNull { id -> loadedDataMap[id] }

                        PropertyUtils.setProperty(wrapper, batchFieldMetadata.property.name, resultList)
                    }
                }
            }
        }

        return wrappers
    }

    // ==================== 一对多关联处理（@AssemblyListField）====================

    private fun <T : Any> collectListLoadRequests(wrappers: List<T>): BatchListAssemblyRequest {
        val loadRequests = mutableMapOf<ListRefSourceKey, MutableSet<Any>>()

        wrappers.forEach { wrapper ->
            val wrapperClass = wrapper::class
            val wrapperMetadata = resolveWrapperMetadata(wrapperClass)
            val entityMetadata = wrapperMetadata.entity

            if (wrapperMetadata.listFields.isEmpty()) return@forEach

            val sourceEntity: Any = if (entityMetadata.isInheritanceMode) {
                wrapper
            } else {
                PropertyUtils.getProperty(wrapper, entityMetadata.property!!.name)
                    ?: return@forEach
            }

            wrapperMetadata.listFields.forEach { (_, listFieldMetadata) ->
                val sourceFieldName = listFieldMetadata.refSourceField.sourceFieldName.ifEmpty { "id" }
                val sourceFieldValue = PropertyUtils.getProperty(sourceEntity, sourceFieldName)

                if (sourceFieldValue != null) {
                    val queryTargetClass = listFieldMetadata.sourceClass ?: listFieldMetadata.elementClass
                    val key = ListRefSourceKey(
                        targetClass = queryTargetClass,
                        targetFieldName = listFieldMetadata.refSourceField.property.name,
                    )
                    loadRequests
                        .getOrPut(key) { mutableSetOf() }
                        .add(sourceFieldValue)
                }
            }
        }

        return BatchListAssemblyRequest(loadRequests)
    }

    private fun batchListLoad(
        request: BatchListAssemblyRequest,
        dataProvider: BatchListRefSourceDataProvider
    ): BatchListLoadResult {
        val loadedData = mutableMapOf<ListRefSourceKey, Map<Any, List<Any>>>()

        request.loadRequests.forEach { (key, values) ->
            if (values.isNotEmpty()) {
                @Suppress("UNCHECKED_CAST")
                val result = dataProvider.loadListBatch(
                    target = key.targetClass as KClass<Any>,
                    targetFieldName = key.targetFieldName,
                    sourceFieldValues = values
                )
                loadedData[key] = result
            }
        }

        return BatchListLoadResult(loadedData)
    }

    private fun <T : Any> distributeAndFillList(
        wrappers: List<T>,
        loadResult: BatchListLoadResult,
        dataProvider: BatchListRefSourceDataProvider
    ) {
        wrappers.forEach { wrapper ->
            val wrapperClass = wrapper::class
            val wrapperMetadata = resolveWrapperMetadata(wrapperClass)
            val entityMetadata = wrapperMetadata.entity

            if (wrapperMetadata.listFields.isEmpty()) return@forEach

            val sourceEntity: Any = if (entityMetadata.isInheritanceMode) {
                wrapper
            } else {
                PropertyUtils.getProperty(wrapper, entityMetadata.property!!.name)
                    ?: return@forEach
            }

            wrapperMetadata.listFields.forEach { (_, listFieldMetadata) ->
                val sourceFieldName = listFieldMetadata.refSourceField.sourceFieldName.ifEmpty { "id" }
                val sourceFieldValue = PropertyUtils.getProperty(sourceEntity, sourceFieldName)

                if (sourceFieldValue != null) {
                    val queryTargetClass = listFieldMetadata.sourceClass ?: listFieldMetadata.elementClass
                    val key = ListRefSourceKey(
                        targetClass = queryTargetClass,
                        targetFieldName = listFieldMetadata.refSourceField.property.name,
                    )

                    val listData = loadResult.loadedData[key]?.get(sourceFieldValue)
                    if (listData != null) {
                        val finalList =
                            if (listFieldMetadata.isNestedWrapper && dataProvider is BatchRefSourceDataProvider) {
                                @Suppress("UNCHECKED_CAST")
                                val wrapperList = dataProvider.mapList(
                                    listData,
                                    listFieldMetadata.elementClass as KClass<Any>
                                )
                                assembleBatch(wrapperList, dataProvider)
                            } else {
                                listData
                            }
                        PropertyUtils.setProperty(wrapper, listFieldMetadata.property.name, finalList)
                    }
                }
            }
        }
    }

}
