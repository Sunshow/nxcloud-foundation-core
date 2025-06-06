package nxcloud.foundation.core.data.jpa.repository.support

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.EntityManager
import nxcloud.foundation.core.data.jpa.entity.JpaEntityMetadata
import nxcloud.foundation.core.data.support.context.DataQueryContext
import nxcloud.foundation.core.data.support.context.DataQueryContextHolder
import nxcloud.foundation.core.data.support.enumeration.DataQueryMode
import nxcloud.foundation.core.spring.support.context.SpringContextHelper
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.support.JpaEntityInformation

class AdvancedJpaSupporter<T>(
    private val entityInformation: JpaEntityInformation<T, *>,
    private val entityManager: EntityManager,
) {

    private val logger = KotlinLogging.logger {}

    private val entityMetadata: JpaEntityMetadata by lazy {
        val jpaEntitySupporter = SpringContextHelper.getBean(JpaEntitySupporter::class.java)
        jpaEntitySupporter.getMetadataByEntityType(entityInformation.javaType)!!
    }

    /**
     * 是否启用了软删除
     */
    fun enableSoftDelete(): Boolean {
        return entityMetadata.enableSoftDelete
    }

    fun getDataQueryContext(): DataQueryContext = DataQueryContextHolder.currentOrElse()

    /**
     * 组合 DataQueryContext 包含的额外查询条件
     */
    fun <S : T> composeDataQueryContextSpecification(spec: Specification<S>?): Specification<S>? {
        if (!entityMetadata.enableSoftDelete) {
            return spec
        }

        val context = DataQueryContextHolder.currentOrElse()

        logger.debug { "启用全局软删除处理" }

        var softDeleteSpecification: Specification<S> = Specification.where(spec)

        when (context.queryMode) {
            DataQueryMode.NotDeleted -> {
                softDeleteSpecification = softDeleteSpecification
                    .and { root, _, cb ->
                        cb.equal(root.get<Long>("deleted"), 0)
                    }
            }

            DataQueryMode.Deleted -> {
                softDeleteSpecification = softDeleteSpecification
                    .and { root, _, cb ->
                        cb.greaterThan(root.get("deleted"), context.deletedAfter)
                    }

                if (context.deletedBefore > 0) {
                    softDeleteSpecification = softDeleteSpecification
                        .and { root, _, cb ->
                            cb.lessThan(root.get("deleted"), context.deletedBefore)
                        }
                }
            }

            else -> {
            }
        }

        return softDeleteSpecification
    }

}