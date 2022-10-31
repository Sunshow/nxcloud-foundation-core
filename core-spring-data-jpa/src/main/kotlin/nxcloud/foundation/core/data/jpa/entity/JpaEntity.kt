package nxcloud.foundation.core.data.jpa.entity

import nxcloud.foundation.core.data.jpa.listener.JpaEntityLifecycleListenerAdapter
import javax.persistence.EntityListeners
import javax.persistence.MappedSuperclass

@MappedSuperclass
@EntityListeners(JpaEntityLifecycleListenerAdapter::class)
abstract class JpaEntity {
}