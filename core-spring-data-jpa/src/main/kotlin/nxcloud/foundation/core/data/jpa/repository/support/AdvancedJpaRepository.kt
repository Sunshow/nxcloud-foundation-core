package nxcloud.foundation.core.data.jpa.repository.support

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.EntityManager
import jakarta.persistence.NoResultException
import jakarta.persistence.TypedQuery
import jakarta.persistence.criteria.Root
import nxcloud.foundation.core.data.jpa.entity.DeletedField
import nxcloud.foundation.core.data.support.annotation.EnableSoftDelete
import nxcloud.foundation.core.data.support.context.DataQueryContextHolder
import nxcloud.foundation.core.data.support.enumeration.DataQueryMode
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.data.domain.Example
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.provider.PersistenceProvider
import org.springframework.data.jpa.repository.support.CrudMethodMetadata
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.data.repository.query.FluentQuery
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.Assert
import java.util.*
import java.util.function.Function

@Repository
@Transactional(readOnly = true)
class AdvancedJpaRepository<T : Any, ID : Any>(
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

    companion object {
        private const val ID_MUST_NOT_BE_NULL = "The given id must not be null"
    }

    private val enableSoftDelete: EnableSoftDelete? by lazy {
        AnnotationUtils.findAnnotation(entityInformation.javaType, EnableSoftDelete::class.java)
    }

    private val provider: PersistenceProvider by lazy {
        PersistenceProvider.fromEntityManager(entityManager)
    }

    /**
     * 是否启用了软删除
     */
    fun enableSoftDelete(): Boolean {
        return enableSoftDelete != null
    }

    /**
     * 暴露实体类型
     */
    public override fun getDomainClass(): Class<T> {
        return super.getDomainClass()
    }

    private fun applyComment(metadata: CrudMethodMetadata, consumer: (String, Any) -> Unit) {
        metadata.comment
            ?.takeIf {
                provider.commentHintKey != null
            }
            ?.also {
                provider.getCommentHintValue(it)
                    ?.apply {
                        consumer(provider.commentHintKey!!, this)
                    }
            }
    }

    private fun getHints(): Map<String, Any> {
        val hints = mutableMapOf<String, Any>()

        queryHints.withFetchGraphs(entityManager)
            .forEach { t, u ->
                hints[t] = u
            }

        repositoryMethodMetadata
            ?.also {
                applyComment(it) { key, value ->
                    hints[key] = value
                }
            }

        return hints
    }

    override fun <S : T> getCountQuery(spec: Specification<S>?, domainClass: Class<S>): TypedQuery<Long> {
        return super.getCountQuery(composeDataQueryContextSpecification(spec), domainClass)
    }

    override fun <S : T> getQuery(spec: Specification<S>?, domainClass: Class<S>, sort: Sort): TypedQuery<S> {
        return super.getQuery(composeDataQueryContextSpecification(spec), domainClass, sort)
    }

    override fun deleteAllByIdInBatch(ids: MutableIterable<ID>) {
        super.deleteAllByIdInBatch(ids)
    }

    @Transactional
    override fun deleteAllInBatch() {
        super.deleteAllInBatch()
    }

    @Transactional
    override fun deleteAllInBatch(entities: MutableIterable<T>) {
        super.deleteAllInBatch(entities)
    }

    override fun <S : T, R : Any> findBy(
        example: Example<S>,
        queryFunction: Function<FluentQuery.FetchableFluentQuery<S>, R>
    ): R {
        return super.findBy(example, queryFunction)
    }

    override fun <S : T, R : Any> findBy(
        spec: Specification<T>,
        queryFunction: Function<FluentQuery.FetchableFluentQuery<S>, R>
    ): R {
        return super.findBy(spec, queryFunction)
    }

    override fun <S : T> exists(example: Example<S>): Boolean {
        return super.exists(example)
    }

    override fun exists(spec: Specification<T>): Boolean {
        return super.exists(spec)
    }

    override fun delete(spec: Specification<T>): Long {
        return super.delete(spec)
    }

    @Transactional
    override fun delete(entity: T) {
        if (enableSoftDelete() && entity is DeletedField) {
            (entity as DeletedField).deleted = System.currentTimeMillis()
            entityManager.merge(entity)
        } else {
            super.delete(entity)
        }
    }

    override fun existsById(id: ID): Boolean {
        return this.findById(id).isPresent
    }

    override fun findById(id: ID): Optional<T> {
        Assert.notNull(id, ID_MUST_NOT_BE_NULL)

        val cb = entityManager.criteriaBuilder
        val cq = cb.createQuery(domainClass)
        val root: Root<T> = cq.from(domainClass)

        val idPredicate = cb.equal(root.get<Any>(entityInformation.idAttribute.name), id)
        composeDataQueryContextSpecification<T>(null)
            ?.toPredicate(root, cq, cb)
            ?.also {
                cq.where(cb.and(idPredicate, it))
            }
            ?: cq.where(idPredicate)

        val query = entityManager.createQuery(cq)

        repositoryMethodMetadata
            ?.lockModeType
            ?.also {
                query.setLockMode(it)
            }

        getHints()
            .forEach { (k, v) ->
                query.setHint(k, v)
            }

        return try {
            Optional.of(query.singleResult)
        } catch (e: NoResultException) {
            Optional.empty()
        }
    }

    @Transactional
    override fun <S : T> save(entity: S): S {
        return super.save(entity)
    }

    /**
     * 组合 DataQueryContext 包含的额外查询条件
     */
    fun <S : T> composeDataQueryContextSpecification(spec: Specification<S>?): Specification<S>? {
        if (!enableSoftDelete()) {
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