package nxcloud.foundation.core.util

import java.util.Comparator

/**
 * 可选的树构建扩展方法
 *
 * 这些扩展方法需要显式导入才能使用，避免污染 List 的命名空间
 *
 * 使用方式：
 * ```kotlin
 * import nxcloud.foundation.core.util.TreeExtensions.toTree
 *
 * val tree = list.toTree { ... }
 * ```
 */
object TreeExtensions {

    /**
     * DSL 风格的树构建方法
     */
    fun <T, ID> List<T>.toTree(configure: TreeBuilder<T, ID>.() -> Unit): List<TreeNode<T>> {
        return TreeBuilder.build(this, configure)
    }

    /**
     * 简洁的树构建方法（针对 Long 类型 ID）
     */
    fun <T> List<T>.toTreeWithLongId(
        id: (T) -> Long,
        parentId: (T) -> Long,
        sortBy: ((T) -> Comparable<*>)? = null
    ): List<TreeNode<T>> {
        return TreeBuilder.buildWithLongId(this, id, parentId, sortBy)
    }

    /**
     * 简洁的树构建方法（针对 String 类型 ID）
     */
    fun <T> List<T>.toTreeWithStringId(
        id: (T) -> String,
        parentId: (T) -> String,
        sortBy: ((T) -> Comparable<*>)? = null
    ): List<TreeNode<T>> {
        return TreeBuilder.buildWithStringId(this, id, parentId, sortBy)
    }

    /**
     * 字符串 ID 的简化版本（以空字符串作为顶层判断）
     */
    fun <T> List<T>.toTreeNodesWithStringId(
        id: (T) -> String,
        parentId: (T) -> String,
        comparator: Comparator<in T> = Comparator { _, _ -> 0 }
    ): List<TreeNode<T>> {
        return TreeBuilder.buildTreeNodes(this, id, parentId, { it.isEmpty() }, comparator)
    }

    /**
     * Long ID 的简化版本（以 0L 作为顶层判断）
     */
    fun <T> List<T>.toTreeNodesWithLongId(
        id: (T) -> Long,
        parentId: (T) -> Long,
        comparator: Comparator<in T> = Comparator { _, _ -> 0 }
    ): List<TreeNode<T>> {
        return TreeBuilder.buildTreeNodes(this, id, parentId, { it == 0L }, comparator)
    }
}