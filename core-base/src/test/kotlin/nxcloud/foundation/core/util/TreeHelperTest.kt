package nxcloud.foundation.core.util

import nxcloud.foundation.core.util.TreeHelper.buildTree
import org.junit.jupiter.api.Test

internal class TreeHelperTest {

    @Test
    fun testBuildTree() {
        val tree = buildTree(
            treeData(),
            TreeNode::id,
            TreeNode::parentId,
            String::isEmpty,
            { node, children -> node.children = children },
            Comparator.comparing(TreeNode::sortValue),
        )

        println(tree)
    }

    private fun treeData(): List<TreeNode> {
        val data = listOf(
            TreeNode("1", "Node 1", "", 0),
            TreeNode("2", "Node 2", "1", 3),
            TreeNode("3", "Node 3", "1", -1),
            TreeNode("4", "Node 4", "", 5),
            TreeNode("5", "Node 5", "-1", 3),
            TreeNode("6", "Node 6", "5", 4)
        )
        println(data)
        return data
    }


    internal data class TreeNode(
        val id: String,
        val name: String,
        val parentId: String,
        val sortValue: Int,
        var children: List<TreeNode> = emptyList()
    )
}