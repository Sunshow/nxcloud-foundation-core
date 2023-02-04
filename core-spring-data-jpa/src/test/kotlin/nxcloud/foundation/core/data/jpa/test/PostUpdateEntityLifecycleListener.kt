package nxcloud.foundation.core.data.jpa.test

import nxcloud.foundation.core.data.support.listener.EntityLifecycleListener
import org.hibernate.envers.AuditReaderFactory
import org.springframework.orm.jpa.EntityManagerHolder
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import javax.persistence.EntityManagerFactory


class PostUpdateEntityLifecycleListener(
    private val entityManagerFactory: EntityManagerFactory,
) : EntityLifecycleListener {

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

    override fun onPostPersist(entity: Any) {
        println("PostUpdateEntityLifecycleListener: onPostPersist")

        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun beforeCommit(readOnly: Boolean) {
                println("PostUpdateEntityLifecycleListener: onPostPersist, beforeCommit")
            }

            override fun afterCommit() {
                println("PostUpdateEntityLifecycleListener: onPostPersist, afterCommit")

                if (entity is Employee) {

                    val resource =
                        TransactionSynchronizationManager.getResource(entityManagerFactory) as EntityManagerHolder

                    val auditReader = AuditReaderFactory.get(resource.entityManager)

                    auditReader.getRevisions(Employee::class.java, entity.id)
                        .forEach {
                            val auditedEmployee = auditReader.find(Employee::class.java, entity.id, it)
                            println("Employee [$auditedEmployee] at revision [$it].")
                        }
                }
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