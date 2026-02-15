package nxcloud.foundation.core.assembler.component

import nxcloud.foundation.core.assembler.annotation.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.reflect.KClass

class AssemblableWrapperTest {

    @Test
    fun testAssemble() {
        val refEntity = RefEntity(
            id = 100L,
            description = "测试描述"
        )

        val sourceEntity = SourceEntity(
            id = 1L,
            name = "测试来源",
            refId = 100L
        )

        val wrapper = WrapperEntity(
            source = sourceEntity,
            ref = null,
            description = null
        )

        val assembled = assembleWithReferences(
            wrapper = wrapper,
            references = mapOf(RefEntity::class to refEntity)
        )

        assertNotNull(assembled.source, "源实体不能为空")
        assertEquals(1L, assembled.source.id, "源实体 id 应匹配")
        assertEquals("测试来源", assembled.source.name, "源实体 name 应匹配")

        assertNotNull(assembled.ref, "ref 字段应被填充")
        assertEquals(100L, assembled.ref?.id, "ref.id 应匹配")
        assertEquals("测试描述", assembled.ref?.description, "ref.description 应匹配")

        assertEquals("测试描述", assembled.description, "description 字段应从 ref 中提取")
    }

    @Test
    fun testAssembleJava() {
        val refEntity = TestJavaRefEntity(100L, "测试描述")

        val sourceEntity = TestJavaSourceEntity(1L, "测试来源", 100L)

        val wrapper = TestJavaWrapperEntity()
            .apply {
                source = sourceEntity
            }

        val assembled = assembleWithReferences(
            wrapper = wrapper,
            references = mapOf(TestJavaRefEntity::class to refEntity)
        )

        assertNotNull(assembled.source, "源实体不能为空")
        assertEquals(1L, assembled.source.id, "源实体 id 应匹配")
        assertEquals("测试来源", assembled.source.name, "源实体 name 应匹配")

        assertNotNull(assembled.ref, "ref 字段应被填充")
        assertEquals(100L, assembled.ref.id, "ref.id 应匹配")
        assertEquals("测试描述", assembled.ref.description, "ref.description 应匹配")

        assertEquals("测试描述", assembled.description, "description 字段应从 ref 中提取")
    }

    @Test
    fun testAssembleMixed() {
        val refEntity = TestJavaRefEntity(100L, "测试描述")

        val sourceEntity = TestJavaSourceEntity(1L, "测试来源", 100L)

        val wrapper = MixedWrapperEntity(
            source = sourceEntity,
            ref = null,
            description = null
        )

        val assembled = assembleWithReferences(
            wrapper = wrapper,
            references = mapOf(TestJavaRefEntity::class to refEntity)
        )

        assertNotNull(assembled.source, "源实体不能为空")
        assertEquals(1L, assembled.source.id, "源实体 id 应匹配")
        assertEquals("测试来源", assembled.source.name, "源实体 name 应匹配")

        assertNotNull(assembled.ref, "ref 字段应被填充")
        assertEquals(100L, assembled.ref?.id, "ref.id 应匹配")
        assertEquals("测试描述", assembled.ref?.description, "ref.description 应匹配")

        assertEquals("测试描述", assembled.description, "description 字段应从 ref 中提取")
    }

    @Test
    fun testAssembleBatch() {
        val refEntity1 = RefEntity(id = 100L, description = "描述1")
        val refEntity2 = RefEntity(id = 200L, description = "描述2")
        val refEntity3 = RefEntity(id = 300L, description = "描述3")

        val sourceEntity1 = SourceEntity(id = 1L, name = "来源1", refId = 100L)
        val sourceEntity2 = SourceEntity(id = 2L, name = "来源2", refId = 200L)
        val sourceEntity3 = SourceEntity(id = 3L, name = "来源3", refId = 100L)

        val wrappers = listOf(
            WrapperEntity(source = sourceEntity1),
            WrapperEntity(source = sourceEntity2),
            WrapperEntity(source = sourceEntity3)
        )

        var batchLoadCallCount = 0

        val assembler = AssemblableWrapperAssembler()
        val assembled = assembler.assembleBatch(
            wrappers,
            object : AssemblableWrapperAssembler.BatchRefSourceDataProvider {
                override fun <T : Any> loadBatch(
                    source: KClass<T>,
                    metadata: AssemblableWrapperAssembler.RefSourceFieldMetadata,
                    sourceFieldValues: Set<Any>
                ): Map<Any, T> {
                    batchLoadCallCount++
                    val references = mapOf(
                        100L to refEntity1,
                        200L to refEntity2,
                        300L to refEntity3
                    )
                    @Suppress("UNCHECKED_CAST")
                    return sourceFieldValues
                        .mapNotNull { id -> references[id]?.let { id to it } }
                        .toMap() as Map<Any, T>
                }
            }
        )

        assertEquals(1, batchLoadCallCount, "批量加载应只调用一次")
        assertEquals(3, assembled.size, "应返回3个 Wrapper")

        assertNotNull(assembled[0].ref, "wrapper1.ref 应被填充")
        assertEquals(100L, assembled[0].ref?.id, "wrapper1.ref.id 应匹配")
        assertEquals("描述1", assembled[0].description, "wrapper1.description 应匹配")

        assertNotNull(assembled[1].ref, "wrapper2.ref 应被填充")
        assertEquals(200L, assembled[1].ref?.id, "wrapper2.ref.id 应匹配")
        assertEquals("描述2", assembled[1].description, "wrapper2.description 应匹配")

        assertNotNull(assembled[2].ref, "wrapper3.ref 应被填充")
        assertEquals(100L, assembled[2].ref?.id, "wrapper3.ref.id 应与 wrapper1 相同")
        assertEquals("描述1", assembled[2].description, "wrapper3.description 应与 wrapper1 相同")
    }

    @Test
    fun testAssembleBatchEmpty() {
        val assembler = AssemblableWrapperAssembler()
        val emptyList = emptyList<WrapperEntity>()

        val assembled = assembler.assembleBatch(
            emptyList,
            object : AssemblableWrapperAssembler.BatchRefSourceDataProvider {
                override fun <T : Any> loadBatch(
                    source: KClass<T>,
                    metadata: AssemblableWrapperAssembler.RefSourceFieldMetadata,
                    sourceFieldValues: Set<Any>
                ): Map<Any, T> {
                    throw AssertionError("不应调用 loadBatch")
                }
            }
        )

        assertEquals(0, assembled.size, "空列表应返回空列表")
    }

    @Test
    fun testAssembleInheritanceMode() {
        val refEntity = RefEntity(
            id = 100L,
            description = "继承模式测试描述"
        )

        val wrapper = InheritanceWrapperEntity(
            id = 1L,
            name = "继承模式测试",
            refId = 100L
        )

        val assembled = assembleWithReferences(
            wrapper = wrapper,
            references = mapOf(RefEntity::class to refEntity)
        )

        assertEquals(1L, assembled.id, "id 应匹配")
        assertEquals("继承模式测试", assembled.name, "name 应匹配")

        assertNotNull(assembled.ref, "ref 字段应被填充")
        assertEquals(100L, assembled.ref?.id, "ref.id 应匹配")
        assertEquals("继承模式测试描述", assembled.ref?.description, "ref.description 应匹配")

        assertEquals("继承模式测试描述", assembled.description, "description 字段应从 ref 中提取")
    }

    @Test
    fun testAssembleBatchInheritanceMode() {
        val refEntity1 = RefEntity(id = 100L, description = "描述1")
        val refEntity2 = RefEntity(id = 200L, description = "描述2")

        val wrappers = listOf(
            InheritanceWrapperEntity(id = 1L, name = "来源1", refId = 100L),
            InheritanceWrapperEntity(id = 2L, name = "来源2", refId = 200L),
            InheritanceWrapperEntity(id = 3L, name = "来源3", refId = 100L)
        )

        var batchLoadCallCount = 0

        val assembler = AssemblableWrapperAssembler()
        val assembled = assembler.assembleBatch(
            wrappers,
            object : AssemblableWrapperAssembler.BatchRefSourceDataProvider {
                override fun <T : Any> loadBatch(
                    source: KClass<T>,
                    metadata: AssemblableWrapperAssembler.RefSourceFieldMetadata,
                    sourceFieldValues: Set<Any>
                ): Map<Any, T> {
                    batchLoadCallCount++
                    val references = mapOf(
                        100L to refEntity1,
                        200L to refEntity2
                    )
                    @Suppress("UNCHECKED_CAST")
                    return sourceFieldValues
                        .mapNotNull { id -> references[id]?.let { id to it } }
                        .toMap() as Map<Any, T>
                }
            }
        )

        assertEquals(1, batchLoadCallCount, "批量加载应只调用一次")
        assertEquals(3, assembled.size, "应返回3个 Wrapper")

        assertEquals(1L, assembled[0].id, "wrapper1.id 应匹配")
        assertNotNull(assembled[0].ref, "wrapper1.ref 应被填充")
        assertEquals("描述1", assembled[0].description, "wrapper1.description 应匹配")

        assertEquals(2L, assembled[1].id, "wrapper2.id 应匹配")
        assertNotNull(assembled[1].ref, "wrapper2.ref 应被填充")
        assertEquals("描述2", assembled[1].description, "wrapper2.description 应匹配")

        assertEquals(3L, assembled[2].id, "wrapper3.id 应匹配")
        assertNotNull(assembled[2].ref, "wrapper3.ref 应被填充")
        assertEquals("描述1", assembled[2].description, "wrapper3.description 应匹配")
    }

    @Test
    fun testAssembleInheritanceFromJava() {
        val refEntity = TestJavaRefEntity(100L, "Java继承模式测试描述")

        val wrapper = JavaInheritanceWrapperEntity(
            id = 1L,
            name = "Java继承模式测试",
            refId = 100L
        )

        val assembled = assembleWithReferences(
            wrapper = wrapper,
            references = mapOf(TestJavaRefEntity::class to refEntity)
        )

        assertEquals(1L, assembled.id, "id 应匹配")
        assertEquals("Java继承模式测试", assembled.name, "name 应匹配")

        assertNotNull(assembled.ref, "ref 字段应被填充")
        assertEquals(100L, assembled.ref?.id, "ref.id 应匹配")
        assertEquals("Java继承模式测试描述", assembled.ref?.description, "ref.description 应匹配")

        assertEquals("Java继承模式测试描述", assembled.description, "description 字段应从 ref 中提取")
    }

    @Test
    fun testAssembleBatchInheritanceFromJava() {
        val refEntity1 = TestJavaRefEntity(100L, "描述1")
        val refEntity2 = TestJavaRefEntity(200L, "描述2")

        val wrappers = listOf(
            JavaInheritanceWrapperEntity(1L, "来源1", 100L),
            JavaInheritanceWrapperEntity(2L, "来源2", 200L),
            JavaInheritanceWrapperEntity(3L, "来源3", 100L)
        )

        var batchLoadCallCount = 0

        val assembler = AssemblableWrapperAssembler()
        val assembled = assembler.assembleBatch(
            wrappers,
            object : AssemblableWrapperAssembler.BatchRefSourceDataProvider {
                override fun <T : Any> loadBatch(
                    source: KClass<T>,
                    metadata: AssemblableWrapperAssembler.RefSourceFieldMetadata,
                    sourceFieldValues: Set<Any>
                ): Map<Any, T> {
                    batchLoadCallCount++
                    val references = mapOf(
                        100L to refEntity1,
                        200L to refEntity2
                    )
                    @Suppress("UNCHECKED_CAST")
                    return sourceFieldValues
                        .mapNotNull { id -> references[id]?.let { id to it } }
                        .toMap() as Map<Any, T>
                }
            }
        )

        assertEquals(1, batchLoadCallCount, "批量加载应只调用一次")
        assertEquals(3, assembled.size, "应返回3个 Wrapper")

        assertEquals(1L, assembled[0].id, "wrapper1.id 应匹配")
        assertEquals("描述1", assembled[0].description, "wrapper1.description 应匹配")

        assertEquals(2L, assembled[1].id, "wrapper2.id 应匹配")
        assertEquals("描述2", assembled[1].description, "wrapper2.description 应匹配")

        assertEquals(3L, assembled[2].id, "wrapper3.id 应匹配")
        assertEquals("描述1", assembled[2].description, "wrapper3.description 应匹配")
    }

    @Test
    fun testAssembleJavaInheritanceMode() {
        val refEntity = TestJavaRefEntity(100L, "Java Wrapper继承模式测试描述")

        val wrapper = TestJavaInheritanceWrapperEntity(1L, "Java Wrapper继承模式测试", 100L)

        val assembled = assembleWithReferences(
            wrapper = wrapper,
            references = mapOf(TestJavaRefEntity::class to refEntity)
        )

        assertEquals(1L, assembled.id, "id 应匹配")
        assertEquals("Java Wrapper继承模式测试", assembled.name, "name 应匹配")

        assertNotNull(assembled.ref, "ref 字段应被填充")
        assertEquals(100L, assembled.ref.id, "ref.id 应匹配")
        assertEquals("Java Wrapper继承模式测试描述", assembled.ref.description, "ref.description 应匹配")

        assertEquals("Java Wrapper继承模式测试描述", assembled.description, "description 字段应从 ref 中提取")
    }

    @Test
    fun testAssembleBatchJavaInheritanceMode() {
        val refEntity1 = TestJavaRefEntity(100L, "描述1")
        val refEntity2 = TestJavaRefEntity(200L, "描述2")

        val wrappers = listOf(
            TestJavaInheritanceWrapperEntity(1L, "来源1", 100L),
            TestJavaInheritanceWrapperEntity(2L, "来源2", 200L),
            TestJavaInheritanceWrapperEntity(3L, "来源3", 100L)
        )

        var batchLoadCallCount = 0

        val assembler = AssemblableWrapperAssembler()
        val assembled = assembler.assembleBatch(
            wrappers,
            object : AssemblableWrapperAssembler.BatchRefSourceDataProvider {
                override fun <T : Any> loadBatch(
                    source: KClass<T>,
                    metadata: AssemblableWrapperAssembler.RefSourceFieldMetadata,
                    sourceFieldValues: Set<Any>
                ): Map<Any, T> {
                    batchLoadCallCount++
                    val references = mapOf(
                        100L to refEntity1,
                        200L to refEntity2
                    )
                    @Suppress("UNCHECKED_CAST")
                    return sourceFieldValues
                        .mapNotNull { id -> references[id]?.let { id to it } }
                        .toMap() as Map<Any, T>
                }
            }
        )

        assertEquals(1, batchLoadCallCount, "批量加载应只调用一次")
        assertEquals(3, assembled.size, "应返回3个 Wrapper")

        assertEquals(1L, assembled[0].id, "wrapper1.id 应匹配")
        assertEquals("描述1", assembled[0].description, "wrapper1.description 应匹配")

        assertEquals(2L, assembled[1].id, "wrapper2.id 应匹配")
        assertEquals("描述2", assembled[1].description, "wrapper2.description 应匹配")

        assertEquals(3L, assembled[2].id, "wrapper3.id 应匹配")
        assertEquals("描述1", assembled[2].description, "wrapper3.description 应匹配")
    }

    @Test
    fun testAssembleListFieldBatch() {
        val child1 = ChildEntity(id = 1L, name = "子项1", parentId = 100L)
        val child2 = ChildEntity(id = 2L, name = "子项2", parentId = 100L)
        val child3 = ChildEntity(id = 3L, name = "子项3", parentId = 200L)

        val wrappers = listOf(
            ParentWithChildrenWrapperEntity(id = 100L, name = "父项1"),
            ParentWithChildrenWrapperEntity(id = 200L, name = "父项2"),
        )

        var batchLoadCallCount = 0
        var listBatchLoadCallCount = 0

        val combinedProvider = object : AssemblableWrapperAssembler.BatchRefSourceDataProvider,
            AssemblableWrapperAssembler.BatchListRefSourceDataProvider {
            override fun <T : Any> loadBatch(
                source: KClass<T>,
                metadata: AssemblableWrapperAssembler.RefSourceFieldMetadata,
                sourceFieldValues: Set<Any>
            ): Map<Any, T> {
                batchLoadCallCount++
                return emptyMap()
            }

            override fun <T : Any> loadListBatch(
                target: KClass<T>,
                targetFieldName: String,
                sourceFieldValues: Set<Any>
            ): Map<Any, List<T>> {
                listBatchLoadCallCount++
                assertEquals("parentId", targetFieldName, "目标字段名应为 parentId")

                val allChildren = listOf(child1, child2, child3)
                @Suppress("UNCHECKED_CAST")
                return allChildren
                    .filter { sourceFieldValues.contains(it.parentId) }
                    .groupBy { it.parentId } as Map<Any, List<T>>
            }

            override fun <S : Any, T : Any> mapList(source: List<S>, targetClass: KClass<T>): List<T> {
                @Suppress("UNCHECKED_CAST")
                return source as List<T>
            }
        }

        val assembler = AssemblableWrapperAssembler()
        val assembled = assembler.assembleBatch(wrappers, combinedProvider)

        assertEquals(1, listBatchLoadCallCount, "列表批量加载应只调用一次")
        assertEquals(2, assembled.size, "应返回2个 Wrapper")

        assertNotNull(assembled[0].children, "wrapper1.children 应被填充")
        assertEquals(2, assembled[0].children?.size, "wrapper1.children 应有2个元素")
        assertEquals("子项1", assembled[0].children?.get(0)?.name, "wrapper1.children[0].name 应匹配")
        assertEquals("子项2", assembled[0].children?.get(1)?.name, "wrapper1.children[1].name 应匹配")

        assertNotNull(assembled[1].children, "wrapper2.children 应被填充")
        assertEquals(1, assembled[1].children?.size, "wrapper2.children 应有1个元素")
        assertEquals("子项3", assembled[1].children?.get(0)?.name, "wrapper2.children[0].name 应匹配")
    }

    @Test
    fun testAssembleBatchFieldBatch() {
        val tag1 = TagEntity(id = 1L, name = "标签1")
        val tag2 = TagEntity(id = 2L, name = "标签2")
        val tag3 = TagEntity(id = 3L, name = "标签3")

        val wrappers = listOf(
            EntityWithTagsWrapperEntity(id = 100L, name = "实体1", tagIdList = listOf(1L, 2L)),
            EntityWithTagsWrapperEntity(id = 200L, name = "实体2", tagIdList = listOf(2L, 3L)),
            EntityWithTagsWrapperEntity(id = 300L, name = "实体3", tagIdList = listOf(1L, 3L)),
        )

        var batchLoadCallCount = 0

        val assembler = AssemblableWrapperAssembler()
        val assembled = assembler.assembleBatch(
            wrappers,
            object : AssemblableWrapperAssembler.BatchRefSourceDataProvider {
                override fun <T : Any> loadBatch(
                    source: KClass<T>,
                    metadata: AssemblableWrapperAssembler.RefSourceFieldMetadata,
                    sourceFieldValues: Set<Any>
                ): Map<Any, T> {
                    batchLoadCallCount++
                    assertEquals(TagEntity::class as KClass<*>, source, "应加载 TagEntity")
                    assertEquals(3, sourceFieldValues.size, "应包含3个不同的 ID")

                    val allTags = mapOf(
                        1L to tag1,
                        2L to tag2,
                        3L to tag3
                    )
                    @Suppress("UNCHECKED_CAST")
                    return sourceFieldValues
                        .mapNotNull { id -> allTags[id]?.let { id to it } }
                        .toMap() as Map<Any, T>
                }
            }
        )

        assertEquals(1, batchLoadCallCount, "批量加载应只调用一次")
        assertEquals(3, assembled.size, "应返回3个 Wrapper")

        assertNotNull(assembled[0].tagList, "wrapper1.tagList 应被填充")
        assertEquals(2, assembled[0].tagList?.size, "wrapper1.tagList 应有2个元素")
        assertEquals("标签1", assembled[0].tagList?.get(0)?.name, "wrapper1.tagList[0].name 应匹配")
        assertEquals("标签2", assembled[0].tagList?.get(1)?.name, "wrapper1.tagList[1].name 应匹配")

        assertNotNull(assembled[1].tagList, "wrapper2.tagList 应被填充")
        assertEquals(2, assembled[1].tagList?.size, "wrapper2.tagList 应有2个元素")
        assertEquals("标签2", assembled[1].tagList?.get(0)?.name, "wrapper2.tagList[0].name 应匹配")
        assertEquals("标签3", assembled[1].tagList?.get(1)?.name, "wrapper2.tagList[1].name 应匹配")

        assertNotNull(assembled[2].tagList, "wrapper3.tagList 应被填充")
        assertEquals(2, assembled[2].tagList?.size, "wrapper3.tagList 应有2个元素")
        assertEquals("标签1", assembled[2].tagList?.get(0)?.name, "wrapper3.tagList[0].name 应匹配")
        assertEquals("标签3", assembled[2].tagList?.get(1)?.name, "wrapper3.tagList[1].name 应匹配")
    }

    @Test
    fun testAssembleBatchFieldWithEmptyList() {
        val wrappers = listOf(
            EntityWithTagsWrapperEntity(id = 100L, name = "实体1", tagIdList = emptyList()),
        )

        var batchLoadCallCount = 0

        val assembler = AssemblableWrapperAssembler()
        val assembled = assembler.assembleBatch(
            wrappers,
            object : AssemblableWrapperAssembler.BatchRefSourceDataProvider {
                override fun <T : Any> loadBatch(
                    source: KClass<T>,
                    metadata: AssemblableWrapperAssembler.RefSourceFieldMetadata,
                    sourceFieldValues: Set<Any>
                ): Map<Any, T> {
                    batchLoadCallCount++
                    return emptyMap()
                }
            }
        )

        assertEquals(0, batchLoadCallCount, "空列表不应触发批量加载")
        assertEquals(1, assembled.size, "应返回1个 Wrapper")
    }

    // ========== Helper ==========

    private fun <T : Any> assembleWithReferences(wrapper: T, references: Map<KClass<*>, Any>): T {
        val assembler = AssemblableWrapperAssembler()
        return assembler.assemble(wrapper, object : AssemblableWrapperAssembler.RefSourceDataProvider {
            override fun <T : Any> load(
                source: KClass<T>,
                metadata: AssemblableWrapperAssembler.RefSourceFieldMetadata,
                sourceFieldValue: Any?
            ): T? {
                return references[source] as? T
            }
        })
    }

    // ========== 组合模式测试实体 ==========

    data class SourceEntity(
        val id: Long,
        val name: String,

        @AssemblyRefSource(source = RefEntity::class, sourceField = "id")
        val refId: Long,
    )

    data class RefEntity(
        val id: Long,
        val description: String,
    )

    @AssemblableWrapper
    data class WrapperEntity(
        @field:AssemblyEntity
        val source: SourceEntity,

        @field:AssemblyField
        var ref: RefEntity? = null,

        @field:AssemblyField(entityField = "refId", targetField = "description")
        var description: String? = null,
    )

    @AssemblableWrapper
    data class MixedWrapperEntity(
        @field:AssemblyEntity
        val source: TestJavaSourceEntity,

        @field:AssemblyField
        var ref: TestJavaRefEntity? = null,

        @field:AssemblyField(entityField = "refId", targetField = "description")
        var description: String? = null,
    )

    // ========== 继承模式测试实体 ==========

    open class InheritableSourceEntity(
        open var id: Long = 0L,
        open var name: String = "",

        @field:AssemblyRefSource(source = RefEntity::class, sourceField = "id")
        open var refId: Long = 0L,
    )

    @AssemblableWrapper
    class InheritanceWrapperEntity(
        id: Long = 0L,
        name: String = "",
        refId: Long = 0L,

        @field:AssemblyField
        var ref: RefEntity? = null,

        @field:AssemblyField(entityField = "refId", targetField = "description")
        var description: String? = null,
    ) : InheritableSourceEntity(id, name, refId)

    // Kotlin 继承 Java 类的 Wrapper
    @AssemblableWrapper
    class JavaInheritanceWrapperEntity : TestJavaInheritableEntity {
        @field:AssemblyField
        var ref: TestJavaRefEntity? = null

        @field:AssemblyField(entityField = "refId", targetField = "description")
        var description: String? = null

        constructor() : super()

        constructor(id: Long, name: String, refId: Long) : super(id, name, refId)
    }

    // ========== @AssemblyListField 测试实体 ==========

    open class ParentEntity(
        open var id: Long = 0L,
        open var name: String = "",
    )

    data class ChildEntity(
        val id: Long,
        val name: String,
        @AssemblyRefSource(source = ParentEntity::class)
        val parentId: Long,
    )

    @AssemblableWrapper
    class ParentWithChildrenWrapperEntity(
        id: Long = 0L,
        name: String = "",

        @field:AssemblyListField
        var children: List<ChildEntity>? = null,
    ) : ParentEntity(id, name)

    // ========== @AssemblyBatchField 测试实体 ==========

    data class TagEntity(
        val id: Long,
        val name: String,
    )

    open class EntityWithTagIds(
        open var id: Long = 0L,
        open var name: String = "",

        @AssemblyRefSource(source = TagEntity::class, sourceField = "id")
        open var tagIdList: List<Long> = emptyList(),
    )

    @AssemblableWrapper
    class EntityWithTagsWrapperEntity(
        id: Long = 0L,
        name: String = "",
        tagIdList: List<Long> = emptyList(),

        @field:AssemblyBatchField(entityField = "tagIdList")
        var tagList: List<TagEntity>? = null,
    ) : EntityWithTagIds(id, name, tagIdList)
}
