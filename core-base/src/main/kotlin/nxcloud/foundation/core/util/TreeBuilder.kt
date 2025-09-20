package nxcloud.foundation.core.util

import java.util.Comparator

/**
 * 树构建器 DSL
 *
 * @param T 数据类型
 * @param ID ID 类型
 */
class TreeBuilder<T, ID> {
    private var idSelector: ((T) -> ID)? = null
    private var parentIdSelector: ((T) -> ID)? = null
    private var isTopPredicate: ((ID) -> Boolean)? = null
    private var comparator: Comparator<in T>? = null
    private var childrenSetter: ((T, List<T>) -> Unit)? = null

    /**
     * 设置 ID 选择器
     */
    fun id(selector: (T) -> ID) {
        this.idSelector = selector
    }

    /**
     * 设置父 ID 选择器
     */
    fun parentId(selector: (T) -> ID) {
        this.parentIdSelector = selector
    }

    /**
     * 设置顶层判断条件
     */
    fun isTop(predicate: (ID) -> Boolean) {
        this.isTopPredicate = predicate
    }

    /**
     * 设置排序器
     */
    fun sortBy(comparator: Comparator<in T>) {
        this.comparator = comparator
    }

    /**
     * 设置排序器（通过属性选择器）
     */
    fun <R : Comparable<R>> sortBy(selector: (T) -> R) {
        this.comparator = Comparator.comparing(selector)
    }

    /**
     * 设置 children 的方法（用于修改原对象）
     */
    fun setChildren(setter: (T, List<T>) -> Unit) {
        this.childrenSetter = setter
    }

    /**
     * 构建树（返回 TreeNode 容器）
     */
    fun build(list: List<T>): List<TreeNode<T>> {
        val id = idSelector ?: throw IllegalStateException("ID selector is required")
        val parentId = parentIdSelector ?: throw IllegalStateException("Parent ID selector is required")
        val isTop = isTopPredicate ?: throw IllegalStateException("IsTop predicate is required")
        val sort = comparator ?: Comparator { _, _ -> 0 }

        return buildTreeNodes(list, id, parentId, isTop, sort)
    }

    /**
     * 构建原生树（修改原对象，返回 List<T>）
     */
    fun buildNative(list: List<T>): List<T> {
        val id = idSelector ?: throw IllegalStateException("ID selector is required")
        val parentId = parentIdSelector ?: throw IllegalStateException("Parent ID selector is required")
        val isTop = isTopPredicate ?: throw IllegalStateException("IsTop predicate is required")
        val setter = childrenSetter ?: throw IllegalStateException("Children setter is required for native build")
        val sort = comparator ?: Comparator { _, _ -> 0 }

        return buildTree(list, id, parentId, isTop, setter, sort)
    }

    companion object {
        /**
         * 构建原生树（修改原对象，返回 List<T>）
         */
        @JvmStatic
        fun <T, ID> buildTree(
            list: List<T>,
            id: (T) -> ID,
            parentId: (T) -> ID,
            isTop: (ID) -> Boolean,
            setChildren: (T, List<T>) -> Unit,
            sort: Comparator<in T>,
        ): List<T> {
            // 先排序
            val sorted = list.sortedWith(sort)
            val map = sorted.associateBy(id)
            val childrenMap = mutableMapOf<ID, MutableList<T>>()
            val tree = mutableListOf<T>()

            sorted.forEach {
                val parent = parentId(it)
                if (isTop(parent)) {
                    // 如果是顶层 加入顶层节点
                    tree.add(it)
                } else {
                    // 如果不是顶层 加入到父节点的子节点 父节点不存在的数据直接丢弃
                    map[parent]?.let { _ ->
                        if (childrenMap[parent] == null) {
                            childrenMap[parent] = mutableListOf()
                        }
                        childrenMap[parent]!!.add(it)
                    }
                }
            }

            // 回写 children
            childrenMap.forEach { (id, children) ->
                map[id]?.let {
                    setChildren(it, children)
                }
            }

            return tree.toList()
        }

        /**
         * 构建原生树（Long ID 简化版本）
         */
        @JvmStatic
        fun <T> buildTree(
            list: List<T>,
            id: (T) -> Long,
            parentId: (T) -> Long,
            setChildren: (T, List<T>) -> Unit,
            sort: Comparator<in T>,
        ): List<T> {
            return buildTree(list, id, parentId, { it == 0L }, setChildren, sort)
        }

        /**
         * DSL 风格的原生树构建方法
         */
        @JvmStatic
        fun <T, ID> buildNative(
            list: List<T>,
            configure: TreeBuilder<T, ID>.() -> Unit
        ): List<T> {
            val builder = TreeBuilder<T, ID>()
            builder.configure()
            return builder.buildNative(list)
        }
        /**
         * 构建树结构，返回 TreeNode 容器
         */
        @JvmStatic
        fun <T, ID> buildTreeNodes(
            list: List<T>,
            id: (T) -> ID,
            parentId: (T) -> ID,
            isTop: (ID) -> Boolean,
            sort: Comparator<in T>,
        ): List<TreeNode<T>> {
            // 先排序
            val sorted = list.sortedWith(sort)
            val map = sorted.associateBy(id)
            val childrenMap = mutableMapOf<ID, MutableList<T>>()
            val roots = mutableListOf<T>()

            // 构建父子关系
            sorted.forEach { item ->
                val parent = parentId(item)
                if (isTop(parent)) {
                    roots.add(item)
                } else {
                    map[parent]?.let {
                        childrenMap.computeIfAbsent(parent) { mutableListOf() }.add(item)
                    }
                }
            }

            // 递归构建 TreeNode
            fun buildNode(item: T): TreeNode<T> {
                val children = childrenMap[id(item)]?.map { buildNode(it) } ?: emptyList()
                return TreeNode(item, children)
            }

            return roots.map { buildNode(it) }
        }

        /**
         * 构建树结构，返回 TreeNode 容器（Long ID 简化版本）
         */
        @JvmStatic
        @JvmName("buildTreeNodesWithLongId")
        fun <T> buildTreeNodes(
            list: List<T>,
            id: (T) -> Long,
            parentId: (T) -> Long,
            sort: Comparator<in T>,
        ): List<TreeNode<T>> {
            return buildTreeNodes(list, id, parentId, { it == 0L }, sort)
        }

        /**
         * 构建树结构，返回 TreeNode 容器（String ID 简化版本）
         */
        @JvmStatic
        @JvmName("buildTreeNodesWithStringId")
        fun <T> buildTreeNodes(
            list: List<T>,
            id: (T) -> String,
            parentId: (T) -> String,
            sort: Comparator<in T>,
        ): List<TreeNode<T>> {
            return buildTreeNodes(list, id, parentId, { it.isEmpty() }, sort)
        }
        /**
         * DSL 风格的树构建方法
         */
        @JvmStatic
        fun <T, ID> build(list: List<T>, configure: TreeBuilder<T, ID>.() -> Unit): List<TreeNode<T>> {
            val builder = TreeBuilder<T, ID>()
            builder.configure()
            return builder.build(list)
        }

        /**
         * 简洁的树构建方法（针对 Long 类型 ID）
         */
        @JvmStatic
        fun <T> buildWithLongId(
            list: List<T>,
            id: (T) -> Long,
            parentId: (T) -> Long,
            sortBy: ((T) -> Comparable<*>)? = null
        ): List<TreeNode<T>> {
            return build<T, Long>(list) {
                id(id)
                parentId(parentId)
                isTop { it == 0L }
                if (sortBy != null) {
                    @Suppress("UNCHECKED_CAST")
                    sortBy(sortBy as (T) -> Comparable<Any>)
                }
            }
        }

        /**
         * 简洁的树构建方法（针对 String 类型 ID）
         */
        @JvmStatic
        fun <T> buildWithStringId(
            list: List<T>,
            id: (T) -> String,
            parentId: (T) -> String,
            sortBy: ((T) -> Comparable<*>)? = null
        ): List<TreeNode<T>> {
            return build<T, String>(list) {
                id(id)
                parentId(parentId)
                isTop { it.isEmpty() }
                if (sortBy != null) {
                    @Suppress("UNCHECKED_CAST")
                    sortBy(sortBy as (T) -> Comparable<Any>)
                }
            }
        }

        /**
         * 字符串 ID 的简化版本（以空字符串作为顶层判断）
         */
        @JvmStatic
        fun <T> buildTreeWithStringId(
            list: List<T>,
            id: (T) -> String,
            parentId: (T) -> String,
            comparator: Comparator<in T> = Comparator { _, _ -> 0 }
        ): List<TreeNode<T>> {
            return buildTreeNodes(list, id, parentId, { it.isEmpty() }, comparator)
        }

        /**
         * Long ID 的简化版本（以 0L 作为顶层判断）
         */
        @JvmStatic
        fun <T> buildTreeWithLongId(
            list: List<T>,
            id: (T) -> Long,
            parentId: (T) -> Long,
            comparator: Comparator<in T> = Comparator { _, _ -> 0 }
        ): List<TreeNode<T>> {
            return buildTreeNodes(list, id, parentId, { it == 0L }, comparator)
        }
    }
}