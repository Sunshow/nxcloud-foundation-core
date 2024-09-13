package nxcloud.foundation.core.data.support.context

object DataQueryContextHolder {

    private val empty = DataQueryContext()

    private val contextThreadLocal = ThreadLocal<DataQueryContext>()

    @JvmStatic
    fun current(): DataQueryContext? {
        return contextThreadLocal.get()
    }

    inline fun <reified T : DataQueryContext> currentTyped(): T? {
        return current()
            ?.let {
                it as T
            }
    }

    inline fun <reified T : DataQueryContext> currentOrElseTyped(crossinline supplier: () -> T): T {
        return currentOrElse {
            supplier.invoke()
        }.let {
            it as T
        }
    }

    @JvmStatic
    fun currentOrElse(supplier: () -> DataQueryContext = { empty }): DataQueryContext {
        var context = contextThreadLocal.get()
        if (context == null) {
            synchronized(this) {
                context = contextThreadLocal.get()
                if (context != null) {
                    return context
                }

                context = supplier.invoke()

                contextThreadLocal.set(context)
            }
        }
        return context
    }

    @JvmStatic
    fun set(context: DataQueryContext) {
        contextThreadLocal.set(context)
    }

    @JvmStatic
    fun exists(): Boolean {
        return contextThreadLocal.get() != null
    }

    @JvmStatic
    fun reset() {
        contextThreadLocal.remove()
    }

}