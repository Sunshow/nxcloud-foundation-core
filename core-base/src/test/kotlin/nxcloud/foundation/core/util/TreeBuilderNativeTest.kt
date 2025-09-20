package nxcloud.foundation.core.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.Comparator

internal class TreeBuilderNativeTest {

    @Test
    fun testBuildNativeDSL() {
        val items = mutableListOf(
            MutableTestItem("1", "Item 1", "", 0),
            MutableTestItem("2", "Item 2", "1", 3),
            MutableTestItem("3", "Item 3", "1", -1),
            MutableTestItem("4", "Item 4", "", 5)
        )

        val result = TreeBuilder.buildNative<MutableTestItem, String>(items) {
            id { it.id }
            parentId { it.parentId }
            isTop { it.parentId.isEmpty() }
            sortBy { it.sortValue }
            setChildren { item, children -> item.children = children }
        }

        assertNotNull(result)
        assertEquals(2, result.size, "应该有两个根节点")

        // 验证根节点的children被正确设置
        val firstRoot = result.find { it.id == "1" }
        assertNotNull(firstRoot)
        assertEquals(2, firstRoot!!.children.size, "第一个根节点应该有2个子节点")

        val secondRoot = result.find { it.id == "4" }
        assertNotNull(secondRoot)
        assertEquals(0, secondRoot!!.children.size, "第二个根节点应该没有子节点")
    }

    @Test
    fun testBuildNativeInstanceMethod() {
        val items = mutableListOf(
            MutableTestItem("1", "Item 1", "", 0),
            MutableTestItem("2", "Item 2", "1", 3),
            MutableTestItem("3", "Item 3", "1", -1)
        )

        val builder = TreeBuilder<MutableTestItem, String>()
        builder.id { it.id }
        builder.parentId { it.parentId }
        builder.isTop { it.parentId.isEmpty() }
        builder.sortBy(Comparator.comparing { it.sortValue })
        builder.setChildren { item, children -> item.children = children }

        val result = builder.buildNative(items)

        assertNotNull(result)
        assertEquals(1, result.size, "应该有一个根节点")

        val root = result[0]
        assertEquals("1", root.id)
        assertEquals(2, root.children.size, "根节点应该有2个子节点")

        // 验证子节点按sortValue排序
        assertEquals("3", root.children[0].id, "第一个子节点应该是sortValue最小的")
        assertEquals("2", root.children[1].id, "第二个子节点应该是sortValue较大的")
    }

    @Test
    fun testBuildTreeStaticMethod() {
        val items = mutableListOf(
            MutableTestItem("1", "Item 1", "", 0),
            MutableTestItem("2", "Item 2", "1", 1)
        )

        val result = TreeBuilder.buildTree(
            items,
            { it.id },
            { it.parentId },
            { it.parentId.isEmpty() },
            { item, children -> item.children = children },
            Comparator.comparing { it.sortValue }
        )

        assertNotNull(result)
        assertEquals(1, result.size)

        val root = result[0]
        assertEquals("1", root.id)
        assertEquals(1, root.children.size)
        assertEquals("2", root.children[0].id)
    }

    @Test
    fun testBuildTreeWithLongId() {
        val items = mutableListOf(
            MutableTestItemLong(1L, "Item 1", 0L, 0),
            MutableTestItemLong(2L, "Item 2", 1L, 1)
        )

        val result = TreeBuilder.buildTree(
            items,
            { it.id },
            { it.parentId },
            { item, children -> item.children = children },
            Comparator.comparing { it.sortValue }
        )

        assertNotNull(result)
        assertEquals(1, result.size)

        val root = result[0]
        assertEquals(1L, root.id)
        assertEquals(1, root.children.size)
        assertEquals(2L, root.children[0].id)
    }

    data class MutableTestItem(
        val id: String,
        val name: String,
        val parentId: String,
        val sortValue: Int,
        var children: List<MutableTestItem> = emptyList()
    )

    data class MutableTestItemLong(
        val id: Long,
        val name: String,
        val parentId: Long,
        val sortValue: Int,
        var children: List<MutableTestItemLong> = emptyList()
    )
}