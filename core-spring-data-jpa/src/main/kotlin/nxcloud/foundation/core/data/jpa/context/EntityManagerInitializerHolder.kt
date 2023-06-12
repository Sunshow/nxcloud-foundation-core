package nxcloud.foundation.core.data.jpa.context

import javax.persistence.EntityManager

object EntityManagerInitializerHolder {

    private val initializerThreadLocal: ThreadLocal<List<(EntityManager) -> Unit>> = ThreadLocal()

    fun reset() {
        initializerThreadLocal.remove()
    }

    fun get(): List<(EntityManager) -> Unit> {
        return initializerThreadLocal.get() ?: emptyList()
    }

    fun register(initializer: (EntityManager) -> Unit) {
        val initializers = initializerThreadLocal.get() ?: emptyList()
        initializerThreadLocal.set(initializers + initializer)
    }

    fun unregister(initializer: (EntityManager) -> Unit) {
        val initializers = initializerThreadLocal.get() ?: emptyList()
        initializerThreadLocal.set(initializers - initializer)
    }

}