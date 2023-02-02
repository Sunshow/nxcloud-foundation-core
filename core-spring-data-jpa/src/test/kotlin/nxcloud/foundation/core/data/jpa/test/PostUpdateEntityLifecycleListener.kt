package nxcloud.foundation.core.data.jpa.test

import nxcloud.foundation.core.data.support.listener.EntityLifecycleListener
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager


class PostUpdateEntityLifecycleListener : EntityLifecycleListener {

    override fun onPostUpdate(entity: Any) {
        println("PostUpdateEntityLifecycleListener: onPostUpdate")

        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun beforeCommit(readOnly: Boolean) {
                println("PostUpdateEntityLifecycleListener: onPostUpdate, beforeCommit")
            }

            override fun afterCommit() {
                println("PostUpdateEntityLifecycleListener: onPostUpdate, afterCommit")
            }

            override fun beforeCompletion() {
                println("PostUpdateEntityLifecycleListener: onPostUpdate, beforeCompletion")
            }

            override fun afterCompletion(status: Int) {
                println("PostUpdateEntityLifecycleListener: onPostUpdate, afterCompletion")
            }
        })
    }
}