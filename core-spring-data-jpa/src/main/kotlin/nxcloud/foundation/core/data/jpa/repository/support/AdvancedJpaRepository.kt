package nxcloud.foundation.core.data.jpa.repository.support

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.EntityManager
import nxcloud.foundation.core.data.jpa.entity.DeletedField
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional(readOnly = true)
class AdvancedJpaRepository<T : Any, ID>(
    private val entityInformation: JpaEntityInformation<T, *>,
    private val entityManager: EntityManager,
) : SimpleJpaRepository<T, ID>(entityInformation, entityManager) {

    private val logger = KotlinLogging.logger {}

    constructor(domainClass: Class<T>, em: EntityManager) : this(
        JpaEntityInformationSupport
            .getEntityInformation(
                domainClass,
                em,
            ),
        em,
    )

    private val advancedJpaSupporter = AdvancedJpaSupporter(entityInformation, entityManager)

    /**
     * 暴露实体类型
     */
    public override fun getDomainClass(): Class<T> {
        return super.getDomainClass()
    }

    @JvmSuppressWildcards
    override fun deleteAllByIdInBatch(ids: Iterable<ID>) {
        if (advancedJpaSupporter.enableSoftDelete()) {
            val cb = entityManager.criteriaBuilder
            val update = cb.createCriteriaUpdate(domainClass)
            val root = update.from(domainClass)

            logger.debug {
                "启用软删除, 仅标记删除时间, domainClass=${domainClass}"
            }

            update.set(root.get("deleted"), System.currentTimeMillis())

            val idPredicate = root.get<Any>(entityInformation.idAttribute.name).`in`(ids.toList())

            advancedJpaSupporter.composeDataQueryContextSpecification<T>(null)
                ?.toPredicate(root, cb.createQuery(), cb)
                ?.also {
                    update.where(cb.and(idPredicate, it))
                }
                ?: update.where(idPredicate)

            // Execute the update
            entityManager.createQuery(update).executeUpdate()
        } else {
            super.deleteAllByIdInBatch(ids)
        }
    }

    @Transactional
    override fun deleteAllInBatch() {
        // 全局禁用全表删除
        throw UnsupportedOperationException("全表删除已被禁用")
        // super.deleteAllInBatch()
    }

    @Transactional
    override fun deleteAll() {
        // 全局禁用全表删除
        throw UnsupportedOperationException("全表删除已被禁用")
        // super.deleteAll()
    }

    @JvmSuppressWildcards
    @Transactional
    override fun deleteAllInBatch(entities: Iterable<T>) {
        if (advancedJpaSupporter.enableSoftDelete()) {
            logger.debug {
                "启用软删除, 仅标记删除时间, domainClass=${domainClass}"
            }
            val deletedTime = System.currentTimeMillis()
            entities
                .forEach {
                    if (it is DeletedField) {
                        (it as DeletedField).deleted = deletedTime
                        entityManager.merge(it)
                    }
                }
        } else {
            super.deleteAllInBatch(entities)
        }
    }

    override fun delete(spec: Specification<T>): Long {
        if (advancedJpaSupporter.enableSoftDelete()) {
            val cb = entityManager.criteriaBuilder
            val update = cb.createCriteriaUpdate(domainClass)
            val root = update.from(domainClass)

            logger.debug {
                "启用软删除, 仅标记删除时间, domainClass=${domainClass}"
            }

            update.set(root.get("deleted"), System.currentTimeMillis())

            advancedJpaSupporter.composeDataQueryContextSpecification(spec)!!
                .toPredicate(root, cb.createQuery(), cb)
                .also {
                    update.where(it)
                }

            // Execute the update
            return entityManager.createQuery(update).executeUpdate().toLong()
        } else {
            return super.delete(advancedJpaSupporter.composeDataQueryContextSpecification(spec)!!)
        }
    }

    @Transactional
    override fun delete(entity: T) {
        if (advancedJpaSupporter.enableSoftDelete() && entity is DeletedField) {
            logger.debug {
                "启用软删除, 仅标记删除时间, domainClass=${domainClass}"
            }
            (entity as DeletedField).deleted = System.currentTimeMillis()
            entityManager.merge(entity)
        } else {
            super.delete(entity)
        }
    }
}