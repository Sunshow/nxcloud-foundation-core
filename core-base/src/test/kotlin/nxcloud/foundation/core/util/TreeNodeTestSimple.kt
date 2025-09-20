package nxcloud.foundation.core.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class TreeNodeTestSimple {

    @Test
    fun testTreeNodeCreation() {
        val node = TreeNode("test data")
        assertEquals("test data", node.data)
        assertTrue(node.children.isEmpty())
    }

    @Test
    fun testTreeHelperBuildTreeNodes() {
        val items = listOf(
            TestItem("1", "Item 1", "", 0),
            TestItem("2", "Item 2", "1", 3),
            TestItem("3", "Item 3", "1", -1),
            TestItem("4", "Item 4", "", 5)
        )

        val result = TreeBuilder.buildTreeNodes(
            items,
            { item -> item.id },
            { item -> item.parentId },
            { parentId -> parentId.isEmpty() },
            Comparator.comparing { item -> item.sortValue }
        )

        assertNotNull(result)
        assertEquals(2, result.size, "应该有两个根节点")

        // 访问第一个根节点的数据
        val firstRoot = result[0]
        assertNotNull(firstRoot)
        assertNotNull(firstRoot.data)
        println("First root: ${firstRoot.data.id} - ${firstRoot.data.name}")

        // 验证根节点是否为 ID "1" 或 "4"
        assertTrue(firstRoot.data.id == "1" || firstRoot.data.id == "4")
    }

    data class TestItem(
        val id: String,
        val name: String,
        val parentId: String,
        val sortValue: Int
    )
}