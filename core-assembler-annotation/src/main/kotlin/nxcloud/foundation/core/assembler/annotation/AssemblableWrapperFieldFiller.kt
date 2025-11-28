package nxcloud.foundation.core.assembler.annotation

/**
 * 用于填充 AssemblableWrapper 注解标记的类中的字段的填充器接口
 */
interface AssemblableWrapperFieldFiller {

    /**
     * 填充方法
     * @param context 填充器上下文
     * @return 返回 true 表示在上下文存储了填充结果由调用者全局填充, 返回 false 表示由填充器自行完成填充
     */
    fun fill(context: AssemblableWrapperFieldFillContext): Boolean

}

/**
 * 用于填充 AssemblableWrapper 注解标记的类中的字段的填充器上下文
 */
data class AssemblableWrapperFieldFillContext(
    val wrapper: Any,
    val entity: Any?,
    val wrapperAnnotation: AssemblableWrapper,
    val fieldAnnotation: AssemblyField,
    /**
     * 填充数据
     */
    var filled: Any? = null,
)