package nxcloud.foundation.core.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class SimpleTreeTest {

    @Test
    fun testTreeNodeBasic() {
        val data = "test data"
        val node = TreeNode(data)

        assertEquals(data, node.data)
        assertTrue(node.children.isEmpty())
        assertTrue(node.isLeaf())
        assertEquals(1, node.size())
        assertEquals(1, node.depth())
    }

    @Test
    fun testTreeNodeWithChildren() {
        val root = TreeNode("root")
        val child1 = TreeNode("child1")
        val child2 = TreeNode("child2")

        val tree = TreeNode("root", listOf(child1, child2))

        assertEquals("root", tree.data)
        assertEquals(2, tree.children.size)
        assertFalse(tree.isLeaf())
        assertEquals(3, tree.size())
        assertEquals(2, tree.depth())
    }
}