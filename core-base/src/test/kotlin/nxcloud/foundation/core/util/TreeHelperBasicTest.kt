package nxcloud.foundation.core.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class TreeHelperBasicTest {

    @Test
    fun testBuildTreeNodesBasic() {
        val data = listOf(
            TestData("1", "Node 1", "", 0),
            TestData("2", "Node 2", "1", 3),
            TestData("3", "Node 3", "1", -1),
            TestData("4", "Node 4", "", 5)
        )

        val result = TreeBuilder.buildTreeNodes(
            data,
            { it.id },
            { it.parentId },
            { it.parentId.isEmpty() },
            Comparator.comparing { it.sortValue }
        )

        // 验证返回类型
        assertNotNull(result)
        assertTrue(result is List<*>)

        // 检查每个元素是否是 TreeNode 类型
        result.forEach { item ->
            assertTrue(item is TreeNode<*>)
            val treeNode = item as TreeNode<TestData>
            assertNotNull(treeNode.data)
            println("Node: ${treeNode.data.id} - ${treeNode.data.name}")
        }

        assertEquals(2, result.size, "应该有两个根节点")
    }

    data class TestData(
        val id: String,
        val name: String,
        val parentId: String,
        val sortValue: Int
    )
}