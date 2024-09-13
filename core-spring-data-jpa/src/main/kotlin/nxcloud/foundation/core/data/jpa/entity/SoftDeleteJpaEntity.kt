package nxcloud.foundation.core.data.jpa.entity

import jakarta.persistence.MappedSuperclass

@MappedSuperclass
abstract class SoftDeleteJpaEntity : DefaultJpaEntity(), DeletedField {

    override var deleted: Long = 0

}