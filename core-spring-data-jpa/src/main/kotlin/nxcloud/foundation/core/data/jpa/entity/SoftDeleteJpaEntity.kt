package nxcloud.foundation.core.data.jpa.entity

import jakarta.persistence.MappedSuperclass
import nxcloud.foundation.core.data.jpa.constant.JpaConstants
import org.hibernate.annotations.Filter
import org.hibernate.annotations.FilterDef

@MappedSuperclass
@FilterDef(
    name = JpaConstants.FILTER_SOFT_DELETE,
    parameters = [],
)
@Filter(name = JpaConstants.FILTER_SOFT_DELETE, condition = "\$FILTER_PLACEHOLDER\$.deleted = 0")
abstract class SoftDeleteJpaEntity : DefaultJpaEntity(), DeletedField {

    override var deleted: Long = 0

}