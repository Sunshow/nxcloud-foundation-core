package nxcloud.foundation.core.data.jpa.entity

import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import nxcloud.foundation.core.data.jpa.constant.JpaConstants
import nxcloud.foundation.core.data.jpa.listener.JpaEntityLifecycleListenerAdapter
import org.hibernate.annotations.Filter
import org.hibernate.annotations.FilterDef
import org.hibernate.annotations.ParamDef

/**
 * 将全局过滤器定义在这里 避免重载的时候到处复制
 */
@MappedSuperclass
@EntityListeners(JpaEntityLifecycleListenerAdapter::class)
@FilterDef(
    name = JpaConstants.FILTER_NOT_DELETED,
)
@Filter(
    name = JpaConstants.FILTER_NOT_DELETED,
    condition = "\$FILTER_PLACEHOLDER\$.deleted = 0",
)
@FilterDef(
    name = JpaConstants.FILTER_DELETED_BEFORE,
    parameters = [
        ParamDef(name = "deletedBefore", type = Long::class),
    ],
)
@Filter(
    name = JpaConstants.FILTER_DELETED_BEFORE,
    condition = "\$FILTER_PLACEHOLDER\$.deleted < :deletedBefore",
)
@FilterDef(
    name = JpaConstants.FILTER_DELETED_AFTER,
    parameters = [
        ParamDef(name = "deletedAfter", type = Long::class),
    ],
)
@Filter(
    name = JpaConstants.FILTER_DELETED_AFTER,
    condition = "\$FILTER_PLACEHOLDER\$.deleted > :deletedAfter",
)
abstract class JpaEntity {
}