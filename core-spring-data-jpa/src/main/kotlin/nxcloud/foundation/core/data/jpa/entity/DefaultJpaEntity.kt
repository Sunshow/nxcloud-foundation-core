package nxcloud.foundation.core.data.jpa.entity

import nxcloud.foundation.core.data.jpa.constant.JpaConstants
import org.hibernate.annotations.GenericGenerator
import java.time.LocalDateTime
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class DefaultJpaEntity : JpaEntity(), LongIdPK, CreatedTimeField, UpdatedTimeField {

    @Id
    @GenericGenerator(name = JpaConstants.ID_GENERATOR_NAME, strategy = JpaConstants.ID_GENERATOR_STRATEGY)
    @GeneratedValue(generator = JpaConstants.ID_GENERATOR_NAME)
    override var id: Long = 0

    override var createdTime: LocalDateTime = LocalDateTime.now()

    override var updatedTime: LocalDateTime = LocalDateTime.now()

}