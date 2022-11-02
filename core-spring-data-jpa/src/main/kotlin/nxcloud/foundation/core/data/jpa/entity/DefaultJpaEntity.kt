package nxcloud.foundation.core.data.jpa.entity

import org.hibernate.annotations.GenericGenerator
import java.time.LocalDateTime
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class DefaultJpaEntity : JpaEntity(), LongIdPK, CreatedTimeField, UpdatedTimeField {

    @Id
    @GenericGenerator(name = "JPA", strategy = "nxcloud.foundation.core.data.jpa.id.DeployContextIdentifierGenerator")
    @GeneratedValue(generator = "JPA")
    override var id: Long = 0

    override var createdTime: LocalDateTime = LocalDateTime.now()

    override var updatedTime: LocalDateTime = LocalDateTime.now()

}