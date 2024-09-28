package nxcloud.foundation.core.data.jpa.entity

import jakarta.persistence.MappedSuperclass
import nxcloud.foundation.core.data.support.annotation.EnableSoftDelete

@EnableSoftDelete
@MappedSuperclass
abstract class SoftDeleteJpaEntity : DefaultJpaEntity(), DeletedField {

    override var deleted: Long = 0

}