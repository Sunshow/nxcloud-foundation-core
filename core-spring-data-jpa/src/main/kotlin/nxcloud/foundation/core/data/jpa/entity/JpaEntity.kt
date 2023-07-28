package nxcloud.foundation.core.data.jpa.entity

import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import nxcloud.foundation.core.data.jpa.listener.JpaEntityLifecycleListenerAdapter

@MappedSuperclass
@EntityListeners(JpaEntityLifecycleListenerAdapter::class)
abstract class JpaEntity {
}