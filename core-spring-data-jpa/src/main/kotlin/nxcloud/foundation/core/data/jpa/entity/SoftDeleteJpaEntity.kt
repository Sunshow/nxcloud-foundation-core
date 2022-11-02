package nxcloud.foundation.core.data.jpa.entity

import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class SoftDeleteJpaEntity : DefaultJpaEntity(), DeletedField {

    override var deleted: Long = 0

}