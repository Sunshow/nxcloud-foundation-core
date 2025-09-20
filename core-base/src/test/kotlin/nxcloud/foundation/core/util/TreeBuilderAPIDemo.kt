package nxcloud.foundation.core.util

import nxcloud.foundation.core.util.TreeExtensions.toTree
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class TreeBuilderAPIDemo {

    @Test
    fun testTreeBuilderCompanionMethods() {
        val items = listOf(
            TestItem("1", "Item 1", "", 0),
            TestItem("2", "Item 2", "1", 3),
            TestItem("3", "Item 3", "1", -1),
            TestItem("4", "Item 4", "", 5)
        )

        // 方式1：使用 TreeBuilder 伴生对象的 DSL 方法
        val result1 = TreeBuilder.build<TestItem, String>(items) {
            id { it.id }
            parentId { it.parentId }
            isTop { it.parentId.isEmpty() }
            sortBy { it.sortValue }
        }

        // 方式2：使用 TreeBuilder 伴生对象的便捷方法
        val result2 = TreeBuilder.buildWithStringId(
            items,
            { it.id },
            { it.parentId },
            { it.sortValue }
        )

        assertEquals(2, result1.size, "DSL 方法应该产生正确的结果")
        assertEquals(2, result2.size, "便捷方法应该产生正确的结果")
    }

    @Test
    fun testTreeExtensionsWithImport() {
        val items = listOf(
            TestItem("1", "Item 1", "", 0),
            TestItem("2", "Item 2", "1", 3),
            TestItem("3", "Item 3", "1", -1),
            TestItem("4", "Item 4", "", 5)
        )

        // 方式3：使用 TreeExtensions 的扩展方法（需要显式导入）
        val result = items.toTree<TestItem, String> {
            id { it.id }
            parentId { it.parentId }
            isTop { it.parentId.isEmpty() }
            sortBy { it.sortValue }
        }

        assertEquals(2, result.size, "扩展方法应该产生正确的结果")
    }

    @Test
    fun testTreeExtensionsExplicitCall() {
        val items = listOf(
            TestItem("1", "Item 1", "", 0),
            TestItem("2", "Item 2", "1", 3)
        )

        // 方式4：显式调用 TreeExtensions 对象的方法
        val result1 = TreeExtensions.run {
            items.toTreeWithStringId(
                { it.id },
                { it.parentId },
                { it.sortValue }
            )
        }

        val result2 = TreeExtensions.run {
            items.toTreeNodesWithStringId(
                { it.id },
                { it.parentId },
                Comparator.comparing { it.sortValue }
            )
        }

        assertEquals(1, result1.size, "toTreeWithStringId 应该产生正确的结果")
        assertEquals(1, result2.size, "toTreeNodesWithStringId 应该产生正确的结果")
    }

    data class TestItem(
        val id: String,
        val name: String,
        val parentId: String,
        val sortValue: Int
    )
}