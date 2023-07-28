package nxcloud.foundation.core.data.jpa.entity

import jakarta.persistence.MappedSuperclass

@MappedSuperclass
//@FilterDef(
//    name = JpaConstants.FILTER_SOFT_DELETE,
//    parameters = [],
//)
//@Filter(name = JpaConstants.FILTER_SOFT_DELETE, condition = "\$FILTER_PLACEHOLDER\$.deleted = 0")
abstract class SoftDeleteJpaEntity : DefaultJpaEntity(), DeletedField {

    override var deleted: Long = 0

}