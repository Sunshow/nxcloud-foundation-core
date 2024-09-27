package nxcloud.foundation.core.data.jpa.repository.support

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.EntityManager
import jakarta.persistence.TypedQuery
import nxcloud.foundation.core.data.jpa.entity.DeletedField
import nxcloud.foundation.core.data.support.annotation.EnableSoftDelete
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.data.repository.query.FluentQuery
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.function.Function

@Repository
@Transactional(readOnly = true)
open class AdvancedJpaRepository<T : Any, ID : Any>(
    entityInformation: JpaEntityInformation<T, *>,
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

    private val enableSoftDelete: EnableSoftDelete? by lazy {
        AnnotationUtils.findAnnotation(entityInformation.javaType, EnableSoftDelete::class.java)
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
    override fun getDomainClass(): Class<T> {
        return super.getDomainClass()
    }

    override fun <S : T> getCountQuery(spec: Specification<S>?, domainClass: Class<S>): TypedQuery<Long> {
        return super.getCountQuery(spec, domainClass)
    }

    override fun <S : T> getQuery(spec: Specification<S>?, domainClass: Class<S>, sort: Sort): TypedQuery<S> {
        return super.getQuery(spec, domainClass, sort)
    }

    override fun <S : T> readPage(
        query: TypedQuery<S>,
        domainClass: Class<S>,
        pageable: Pageable,
        spec: Specification<S>?
    ): Page<S> {
        return super.readPage(query, domainClass, pageable, spec)
    }

    override fun getReferenceById(id: ID): T {
        return super.getReferenceById(id)
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
        return super.existsById(id)
    }

    override fun findById(id: ID): Optional<T> {
        return super.findById(id)
    }

    @Transactional
    override fun <S : T> save(entity: S): S {
        return super.save(entity)
    }
}