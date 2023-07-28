package nxcloud.foundation.core.data.jpa.entity

import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import nxcloud.foundation.core.data.jpa.constant.JpaConstants
import org.hibernate.annotations.GenericGenerator
import java.time.LocalDateTime

@MappedSuperclass
abstract class DefaultJpaEntity : JpaEntity(), LongIdPK, CreatedTimeField, UpdatedTimeField {

    @Id
    @GenericGenerator(name = JpaConstants.ID_GENERATOR_NAME, strategy = JpaConstants.ID_GENERATOR_STRATEGY)
    @GeneratedValue(generator = JpaConstants.ID_GENERATOR_NAME)
    override var id: Long = 0

    override var createdTime: LocalDateTime = LocalDateTime.now()

    override var updatedTime: LocalDateTime = LocalDateTime.now()

}