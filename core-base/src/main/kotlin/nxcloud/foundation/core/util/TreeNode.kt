package nxcloud.foundation.core.util

/**
 * 通用树节点容器
 *
 * @param T 包装的数据类型
 * @property data 实际存储的数据
 * @property children 子节点列表
 */
data class TreeNode<T>(
    val data: T,
    val children: List<TreeNode<T>> = emptyList()
) {

    /**
     * 将树形结构展平为列表（深度优先遍历）
     *
     * @return 展平后的数据列表
     */
    fun flatten(): List<T> {
        val result = mutableListOf<T>()
        flattenTo(result)
        return result
    }

    /**
     * 内部递归方法，将节点数据添加到目标列表
     */
    private fun flattenTo(target: MutableList<T>) {
        target.add(data)
        children.forEach { it.flattenTo(target) }
    }

    /**
     * 在树中查找满足条件的第一个节点
     *
     * @param predicate 查找条件
     * @return 找到的节点，没找到返回 null
     */
    fun find(predicate: (T) -> Boolean): TreeNode<T>? {
        if (predicate(data)) {
            return this
        }

        children.forEach { child ->
            val found = child.find(predicate)
            if (found != null) {
                return found
            }
        }

        return null
    }

    /**
     * 在树中查找满足条件的所有节点
     *
     * @param predicate 查找条件
     * @return 找到的节点列表
     */
    fun findAll(predicate: (T) -> Boolean): List<TreeNode<T>> {
        val result = mutableListOf<TreeNode<T>>()
        findAllTo(result, predicate)
        return result
    }

    /**
     * 内部递归方法，将满足条件的节点添加到目标列表
     */
    private fun findAllTo(target: MutableList<TreeNode<T>>, predicate: (T) -> Boolean) {
        if (predicate(data)) {
            target.add(this)
        }
        children.forEach { it.findAllTo(target, predicate) }
    }

    /**
     * 转换树中的数据类型
     *
     * @param R 目标数据类型
     * @param transform 转换函数
     * @return 转换后的树节点
     */
    fun <R> map(transform: (T) -> R): TreeNode<R> {
        return TreeNode(
            data = transform(data),
            children = children.map { it.map(transform) }
        )
    }

    /**
     * 获取树的深度
     *
     * @return 树的深度
     */
    fun depth(): Int {
        if (children.isEmpty()) {
            return 1
        }
        return 1 + (children.maxOfOrNull { it.depth() } ?: 0)
    }

    /**
     * 获取树中所有节点的数量
     *
     * @return 节点总数
     */
    fun size(): Int {
        return 1 + children.sumOf { it.size() }
    }

    /**
     * 检查是否为叶子节点
     *
     * @return true 如果是叶子节点
     */
    fun isLeaf(): Boolean = children.isEmpty()

    /**
     * 检查是否为根节点（根据父节点判断需要在外部实现）
     */

    /**
     * 遍历树中的所有节点（深度优先）
     *
     * @param action 对每个节点执行的操作
     */
    fun forEach(action: (TreeNode<T>) -> Unit) {
        action(this)
        children.forEach { it.forEach(action) }
    }

    /**
     * 遍历树中的所有数据（深度优先）
     *
     * @param action 对每个数据执行的操作
     */
    fun forEachData(action: (T) -> Unit) {
        action(data)
        children.forEach { it.forEachData(action) }
    }
}

/**
 * 将 TreeNode 列表展平为数据列表
 */
fun <T> List<TreeNode<T>>.flatten(): List<T> {
    return this.flatMap { it.flatten() }
}

/**
 * 在 TreeNode 列表中查找满足条件的第一个节点
 */
fun <T> List<TreeNode<T>>.find(predicate: (T) -> Boolean): TreeNode<T>? {
    this.forEach { root ->
        val found = root.find(predicate)
        if (found != null) {
            return found
        }
    }
    return null
}

/**
 * 在 TreeNode 列表中查找满足条件的所有节点
 */
fun <T> List<TreeNode<T>>.findAll(predicate: (T) -> Boolean): List<TreeNode<T>> {
    return this.flatMap { it.findAll(predicate) }
}

/**
 * 计算所有根节点中的最大深度
 */
fun <T> List<TreeNode<T>>.maxDepth(): Int {
    return this.maxOfOrNull { it.depth() } ?: 0
}

/**
 * 计算所有节点总数
 */
fun <T> List<TreeNode<T>>.totalSize(): Int {
    return this.sumOf { it.size() }
}