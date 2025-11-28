package nxcloud.foundation.core.assembler.component

import nxcloud.foundation.core.assembler.annotation.AssemblableWrapper
import nxcloud.foundation.core.assembler.annotation.AssemblyEntity
import nxcloud.foundation.core.assembler.annotation.AssemblyField
import nxcloud.foundation.core.assembler.annotation.AssemblyRefSource
import org.apache.commons.beanutils.PropertyUtils
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

/**
 * 可组装包装类的装配器
 * 根据注解自动填充包装类中的关联字段
 */
class AssemblableWrapperAssembler {

    data class WrapperMetadata(
        val annotation: AssemblableWrapper,
        val entity: EntityMetadata,
        val fields: Map<String, FieldMetadata>,
    )

    data class EntityMetadata(
        val annotation: AssemblyEntity,
        val property: KProperty1<*, *>,
        val entityClass: KClass<*>,
    )

    data class FieldMetadata(
        val annotation: AssemblyField,
        val property: KProperty1<*, *>,
        val fieldClass: KClass<*>,
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

    /**
     * 引用数据提供者接口
     */
    interface RefSourceDataProvider {
        /**
         * 根据类型和关联字段加载引用实体
         * @param source 实体的类型
         * @param metadata 实体关联的字段信息
         * @param sourceFieldValue 实体关联字段的值
         * @return 加载的引用实体，未找到返回 null
         */
        fun <T : Any> load(source: KClass<T>, metadata: RefSourceFieldMetadata, sourceFieldValue: Any?): T?
    }

    /**
     * 批量引用数据提供者接口
     * 继承 RefSourceDataProvider，提供批量加载能力以避免 N+1 问题
     */
    interface BatchRefSourceDataProvider {
        /**
         * 批量加载引用实体
         * @param source 实体的类型
         * @param metadata 实体关联的字段信息
         * @param sourceFieldValues 实体关联字段的值集合（已去重）
         * @return 以关联字段值为 key，加载的引用实体为 value 的 Map
         */
        fun <T : Any> loadBatch(
            source: KClass<T>,
            metadata: RefSourceFieldMetadata,
            sourceFieldValues: Set<Any>
        ): Map<Any, T>
    }

    /**
     * 引用源的唯一标识 Key
     */
    data class RefSourceKey(
        val sourceClass: KClass<*>,
        val sourceFieldName: String,
        val metadata: RefSourceFieldMetadata
    )

    /**
     * 批量装配请求，收集需要加载的引用数据
     */
    data class BatchAssemblyRequest(
        val loadRequests: Map<RefSourceKey, MutableSet<Any>>
    )

    /**
     * 批量加载结果
     */
    data class BatchLoadResult(
        val loadedData: Map<RefSourceKey, Map<Any, Any>>
    )

    private fun resolveWrapperMetadata(wrapperClass: KClass<*>): WrapperMetadata {
        return metadataCache.computeIfAbsent(wrapperClass) {
            // 解析 @AssemblableWrapper 注解
            val wrapperAnnotation = wrapperClass.findAnnotation<AssemblableWrapper>()
                ?: throw IllegalArgumentException("类 ${wrapperClass.simpleName} 未标记 @AssemblableWrapper 注解")

            // 解析 @AssemblyEntity 注解
            val entityProperty = wrapperClass.memberProperties
                .firstOrNull { property ->
                    property.javaField?.getAnnotation(AssemblyEntity::class.java) != null
                }
                ?: throw IllegalArgumentException("类 ${wrapperClass.simpleName} 中未找到 @AssemblyEntity 标记的字段")
            val entityAnnotation = entityProperty.javaField?.getAnnotation(AssemblyEntity::class.java)
                ?: throw IllegalArgumentException("类 ${wrapperClass.simpleName} 中未找到 @AssemblyEntity 标记的字段")
            val entityClass = entityProperty.returnType.classifier as KClass<*>

            val refSourceMetadata = resolveRefSourceMetadata(entityClass)

            // 解析 @AssemblyField 注解
            val fieldMetadataMap = wrapperClass.memberProperties
                .mapNotNull { property ->
                    val fieldAnnotation = property.javaField?.getAnnotation(AssemblyField::class.java)
                    if (fieldAnnotation != null) {
                        val fieldClass = property.returnType.classifier as KClass<*>
                        // 如果指定了 entityField，需要从该字段的 @AssemblyRefSource 注解获取引用类型
                        val refSourceFieldMetadata = if (fieldAnnotation.entityField.isNotEmpty()) {
                            refSourceMetadata.fields[fieldAnnotation.entityField]
                                ?: throw IllegalArgumentException("在 ${entityClass.simpleName} 中未找到字段 '${fieldAnnotation.entityField}' 的 @AssemblyRefSource 注解")
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
                                ?: throw IllegalArgumentException("在 ${entityClass.simpleName} 中未找到指向 ${fieldClass.simpleName} 的唯一 @AssemblyRefSource 字段")
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

            WrapperMetadata(
                annotation = wrapperAnnotation,
                entity = EntityMetadata(
                    annotation = entityAnnotation,
                    property = entityProperty,
                    entityClass = entityClass,
                ),
                fields = fieldMetadataMap
            )
        }
    }

    private fun resolveRefSourceMetadata(entityClass: KClass<*>): RefSourceMetadata {
        return refSourceCache.computeIfAbsent(entityClass) {
            val fieldMetadataMap = entityClass.memberProperties
                .mapNotNull { property ->
                    val refSourceAnnotation = property.javaField?.getAnnotation(AssemblyRefSource::class.java)
                    if (refSourceAnnotation != null) {
                        property.name to RefSourceFieldMetadata(
                            annotation = refSourceAnnotation,
                            property = property,
                            sourceClass = refSourceAnnotation.source,
                            sourceFieldName = refSourceAnnotation.sourceField,
                        )
                    } else {
                        null
                    }
                }
                .toMap()

            RefSourceMetadata(fields = fieldMetadataMap)
        }
    }

    /**
     * 装配包装类实例，通过数据提供者加载引用数据
     *
     * @param wrapper 待装配的包装类实例
     * @param dataProvider 引用数据提供者
     * @return 装配后的包装类实例
     */
    fun <T : Any> assemble(wrapper: T, dataProvider: RefSourceDataProvider): T {
        val wrapperClass = wrapper::class

        val wrapperMetadata = resolveWrapperMetadata(wrapperClass)

        val entityMetadata = wrapperMetadata.entity

        // 查找源实体字段
        val sourceEntity = PropertyUtils.getProperty(wrapper, entityMetadata.property.name)
            ?: throw IllegalArgumentException("源实体不能为空")

        // 处理每个 @AssemblyField 标记的字段
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
                    // 确定要设置的值
                    val valueToSet = if (fieldMetadata.annotation.targetField.isNotEmpty()) {
                        // 从引用实体中提取指定字段
                        PropertyUtils.getProperty(refEntity, fieldMetadata.annotation.targetField)
                    } else {
                        // 设置整个引用实体
                        refEntity
                    }

                    // 设置值
                    PropertyUtils.setProperty(wrapper, fieldMetadata.property.name, valueToSet)
                }
            }

        return wrapper
    }

    /**
     * 批量装配包装类实例，通过数据提供者批量加载引用数据以避免 N+1 问题
     *
     * @param wrappers 待装配的包装类实例列表
     * @param dataProvider 批量引用数据提供者
     * @return 装配后的包装类实例列表
     */
    fun <T : Any> assembleBatch(wrappers: List<T>, dataProvider: BatchRefSourceDataProvider): List<T> {
        if (wrappers.isEmpty()) return wrappers

        // 1. 收集阶段：收集所有需要加载的引用数据
        val request = collectLoadRequests(wrappers)

        // 2. 加载阶段：批量加载所有引用数据
        val loadResult = batchLoad(request, dataProvider)

        // 3. 分发阶段：将加载的数据分发填充到各个 Wrapper
        return distributeAndFill(wrappers, loadResult)
    }

    /**
     * 收集所有 Wrapper 需要加载的引用数据请求
     */
    private fun <T : Any> collectLoadRequests(wrappers: List<T>): BatchAssemblyRequest {
        val loadRequests = mutableMapOf<RefSourceKey, MutableSet<Any>>()

        wrappers.forEach { wrapper ->
            val wrapperClass = wrapper::class
            val wrapperMetadata = resolveWrapperMetadata(wrapperClass)
            val entityMetadata = wrapperMetadata.entity

            // 获取源实体，跳过源实体为空的 Wrapper
            val sourceEntity = PropertyUtils.getProperty(wrapper, entityMetadata.property.name)
                ?: return@forEach

            // 遍历每个 AssemblyField，收集需要加载的引用 ID
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
        }

        return BatchAssemblyRequest(loadRequests)
    }

    /**
     * 批量加载所有引用数据
     */
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

    /**
     * 将加载的数据分发填充到各个 Wrapper
     */
    private fun <T : Any> distributeAndFill(
        wrappers: List<T>,
        loadResult: BatchLoadResult
    ): List<T> {
        wrappers.forEach { wrapper ->
            val wrapperClass = wrapper::class
            val wrapperMetadata = resolveWrapperMetadata(wrapperClass)
            val entityMetadata = wrapperMetadata.entity

            val sourceEntity = PropertyUtils.getProperty(wrapper, entityMetadata.property.name)
                ?: return@forEach

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
        }

        return wrappers
    }

}
