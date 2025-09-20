package nxcloud.foundation.core.util

object TreeHelper {

    /**
     * 构建树
     */
    @Deprecated(
        message = "Use TreeBuilder.buildTree instead",
        replaceWith = ReplaceWith("TreeBuilder.buildTree(list, id, parentId, isTop, setChildren, sort)")
    )
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
        val sorted = list
            .sortedWith(sort)

        val map = sorted.associateBy(id)

        val childrenMap = mutableMapOf<ID, MutableList<T>>()

        val tree = mutableListOf<T>()

        sorted
            .forEach {
                val parent = parentId(it)

                if (isTop(parent)) {
                    // 如果是顶层 加入顶层节点
                    tree.add(it)
                } else {
                    // 如果不是顶层 加入到父节点的子节点 父节点不存在的数据直接丢弃
                    map[parent]?.let { p ->
                        if (childrenMap[parent] == null) {
                            childrenMap[parent] = mutableListOf()
                        }
                        childrenMap[parent]!!.add(it)
                    }
                }
            }

        // 回写 children
        childrenMap
            .forEach { (id, children) ->
                map[id]?.let {
                    setChildren(it, children)
                }
            }

        return tree.toList()
    }

    @Deprecated(
        message = "Use TreeBuilder.buildTree instead",
        replaceWith = ReplaceWith("TreeBuilder.buildTree(list, id, parentId, setChildren, sort)")
    )
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

}