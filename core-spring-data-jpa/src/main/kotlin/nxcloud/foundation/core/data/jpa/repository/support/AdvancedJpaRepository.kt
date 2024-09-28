package nxcloud.foundation.core.data.jpa.repository.support

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.EntityManager
import jakarta.persistence.NoResultException
import jakarta.persistence.Query
import jakarta.persistence.TypedQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import nxcloud.foundation.core.data.jpa.entity.DeletedField
import nxcloud.foundation.core.data.support.annotation.EnableSoftDelete
import nxcloud.foundation.core.data.support.context.DataQueryContextHolder
import nxcloud.foundation.core.data.support.enumeration.DataQueryMode
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.data.domain.Example
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.convert.QueryByExamplePredicateBuilder
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.provider.PersistenceProvider
import org.springframework.data.jpa.repository.query.EscapeCharacter
import org.springframework.data.jpa.repository.support.CrudMethodMetadata
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery
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

    private var escapeCharacter: EscapeCharacter = EscapeCharacter.DEFAULT

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
     * 反射获取父类的 private doFindBy 方法
     */
    private val doFindByMethod by lazy {
        SimpleJpaRepository::class.java
            .getDeclaredMethod(
                "doFindBy",
                Specification::class.java,
                Class::class.java,
                Function::class.java,
            )
            .also {
                it.isAccessible = true
            }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <S : T, R : Any> executeDoFindBy(
        spec: Specification<T>,
        domainClass: Class<T>,
        queryFunction: Function<FetchableFluentQuery<S>, R>,
    ): R {
        return doFindByMethod.invoke(this, spec, domainClass, queryFunction) as R
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


    /**
     * Applies the given [Specification] to the given [CriteriaQuery].
     *
     * @param spec can be null.
     * @param domainClass must not be null.
     * @param query must not be null.
     */
    private fun <S, U : T> applySpecificationToCriteria(
        spec: Specification<U>?,
        domainClass: Class<U>,
        query: CriteriaQuery<S>,
    ): Root<U> {
        val root = query.from(domainClass)

        if (spec == null) {
            return root
        }

        val builder = entityManager.criteriaBuilder
        val predicate = spec.toPredicate(root, query, builder)

        if (predicate != null) {
            query.where(predicate)
        }

        return root
    }

    private fun <S> applyRepositoryMethodMetadata(query: TypedQuery<S>): TypedQuery<S> {
        val metadata = repositoryMethodMetadata ?: return query

        val type = metadata.lockModeType
        val toReturn = type
            ?.let {
                query.setLockMode(it)
            }
            ?: query

        applyQueryHints(toReturn)

        return toReturn
    }

    private fun applyQueryHints(query: Query) {
        val metadata = repositoryMethodMetadata ?: return

        queryHints.withFetchGraphs(entityManager)
            .forEach { t, u ->
                query
                    .setHint(
                        t,
                        u
                    )
            }
        applyComment(metadata) { key, value ->
            query.setHint(key, value)
        }
    }

    override fun setEscapeCharacter(escapeCharacter: EscapeCharacter) {
        this.escapeCharacter = escapeCharacter
        super.setEscapeCharacter(escapeCharacter)
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
        if (enableSoftDelete()) {
            val cb = entityManager.criteriaBuilder
            val update = cb.createCriteriaUpdate(domainClass)
            val root = update.from(domainClass)

            logger.debug {
                "启用软删除, 仅标记删除时间, domainClass=${domainClass}"
            }

            update.set(root.get("deleted"), System.currentTimeMillis())

            val idPredicate = root.get<Any>(entityInformation.idAttribute.name).`in`(ids.toList())

            composeDataQueryContextSpecification<T>(null)
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

    @Transactional
    override fun deleteAllInBatch(entities: MutableIterable<T>) {
        if (enableSoftDelete()) {
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

    @Suppress("UNCHECKED_CAST")
    override fun <S : T, R : Any> findBy(
        example: Example<S>,
        queryFunction: Function<FetchableFluentQuery<S>, R>
    ): R {
        val spec = ExampleSpecification(example, escapeCharacter)
        val probeType = example.probeType
        return executeDoFindBy(
            composeDataQueryContextSpecification(spec) as (Specification<T>),
            probeType as Class<T>,
            queryFunction,
        )
    }

    override fun <S : T, R : Any> findBy(
        spec: Specification<T>,
        queryFunction: Function<FetchableFluentQuery<S>, R>
    ): R {
        return super.findBy(composeDataQueryContextSpecification(spec)!!, queryFunction)
    }

    override fun <S : T> exists(example: Example<S>): Boolean {
        val spec = ExampleSpecification(
            example,
            this.escapeCharacter
        )
        val cq = entityManager.criteriaBuilder //
            .createQuery(Int::class.java) //
            .select(entityManager.criteriaBuilder.literal(1))

        applySpecificationToCriteria(composeDataQueryContextSpecification(spec), example.probeType, cq)

        val query = applyRepositoryMethodMetadata(entityManager.createQuery(cq))
        return query.setMaxResults(1).resultList.size == 1
    }

    override fun exists(spec: Specification<T>): Boolean {
        return super.exists(composeDataQueryContextSpecification(spec)!!)
    }

    override fun delete(spec: Specification<T>): Long {
        if (enableSoftDelete()) {
            val cb = entityManager.criteriaBuilder
            val update = cb.createCriteriaUpdate(domainClass)
            val root = update.from(domainClass)

            logger.debug {
                "启用软删除, 仅标记删除时间, domainClass=${domainClass}"
            }

            update.set(root.get("deleted"), System.currentTimeMillis())

            composeDataQueryContextSpecification(spec)!!
                .toPredicate(root, cb.createQuery(), cb)
                .also {
                    update.where(it)
                }

            // Execute the update
            return entityManager.createQuery(update).executeUpdate().toLong()
        } else {
            return super.delete(composeDataQueryContextSpecification(spec)!!)
        }
    }

    @Transactional
    override fun delete(entity: T) {
        if (enableSoftDelete() && entity is DeletedField) {
            logger.debug {
                "启用软删除, 仅标记删除时间, domainClass=${domainClass}"
            }
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

    class ExampleSpecification<T> internal constructor(
        private val example: Example<T>,
        private val escapeCharacter: EscapeCharacter,
    ) : Specification<T> {

        override fun toPredicate(root: Root<T>, query: CriteriaQuery<*>, cb: CriteriaBuilder): Predicate? {
            return QueryByExamplePredicateBuilder.getPredicate(root, cb, example, escapeCharacter)
        }

    }
}