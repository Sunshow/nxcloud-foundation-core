package org.springframework.data.jpa.repository.query

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.EntityManager
import org.springframework.data.jpa.repository.QueryRewriter
import org.springframework.data.jpa.repository.query.*
import org.springframework.data.projection.ProjectionFactory
import org.springframework.data.repository.core.NamedQueries
import org.springframework.data.repository.core.RepositoryMetadata
import org.springframework.data.repository.query.QueryLookupStrategy
import org.springframework.data.repository.query.QueryMethod
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider
import org.springframework.data.repository.query.RepositoryQuery
import org.springframework.lang.Nullable
import org.springframework.util.StringUtils
import java.lang.reflect.Method

object AdvancedJpaQueryLookupStrategy {

    private val logger = KotlinLogging.logger {}

    /**
     * A null-value instance used to signal if no declared query could be found. It checks many different formats before
     * falling through to this value object.
     */
    private val NO_QUERY: RepositoryQuery = NoQuery()

    /**
     * Creates a [QueryLookupStrategy] for the given [EntityManager] and [Key].
     *
     * @param em must not be null.
     * @param queryMethodFactory must not be null.
     * @param key may be null.
     * @param evaluationContextProvider must not be null.
     * @param escape must not be null.
     */
    fun create(
        em: EntityManager,
        queryMethodFactory: JpaQueryMethodFactory,
        @Nullable key: QueryLookupStrategy.Key?,
        evaluationContextProvider: QueryMethodEvaluationContextProvider,
        queryRewriterProvider: QueryRewriterProvider,
        escape: EscapeCharacter,
    ): QueryLookupStrategy {
        return when (key ?: QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND) {
            QueryLookupStrategy.Key.CREATE -> CreateQueryLookupStrategy(
                em,
                queryMethodFactory,
                queryRewriterProvider,
                escape
            )

            QueryLookupStrategy.Key.USE_DECLARED_QUERY -> DeclaredQueryLookupStrategy(
                em, queryMethodFactory, evaluationContextProvider,
                queryRewriterProvider
            )

            QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND -> CreateIfNotFoundQueryLookupStrategy(
                em, queryMethodFactory,
                CreateQueryLookupStrategy(em, queryMethodFactory, queryRewriterProvider, escape),
                DeclaredQueryLookupStrategy(em, queryMethodFactory, evaluationContextProvider, queryRewriterProvider),
                queryRewriterProvider
            )

            else -> throw IllegalArgumentException("Unsupported query lookup strategy $key")
        }
    }

    /**
     * Base class for [QueryLookupStrategy] implementations that need access to an [EntityManager].
     *
     * @author Oliver Gierke
     * @author Thomas Darimont
     */
    private abstract class AbstractQueryLookupStrategy(
        private val em: EntityManager,
        private val queryMethodFactory: JpaQueryMethodFactory,
        private val queryRewriterProvider: QueryRewriterProvider
    ) : QueryLookupStrategy {

        override fun resolveQuery(
            method: Method,
            metadata: RepositoryMetadata,
            factory: ProjectionFactory,
            namedQueries: NamedQueries,
        ): RepositoryQuery {
            val queryMethod = queryMethodFactory.build(method, metadata, factory)
            return resolveQuery(queryMethod, queryRewriterProvider.getQueryRewriter(queryMethod), em, namedQueries)
        }

        protected abstract fun resolveQuery(
            method: JpaQueryMethod,
            queryRewriter: QueryRewriter,
            em: EntityManager,
            namedQueries: NamedQueries,
        ): RepositoryQuery
    }

    /**
     * [QueryLookupStrategy] to create a query from the method name.
     *
     * @author Oliver Gierke
     * @author Thomas Darimont
     */
    private class CreateQueryLookupStrategy(
        em: EntityManager,
        queryMethodFactory: JpaQueryMethodFactory,
        queryRewriterProvider: QueryRewriterProvider,
        private val escape: EscapeCharacter,
    ) : AbstractQueryLookupStrategy(em, queryMethodFactory, queryRewriterProvider) {
        public override fun resolveQuery(
            method: JpaQueryMethod,
            queryRewriter: QueryRewriter,
            em: EntityManager,
            namedQueries: NamedQueries,
        ): RepositoryQuery {
            return AdvancedPartTreeJpaQuery(method, em, escape)
        }
    }

    /**
     * [QueryLookupStrategy] that tries to detect a declared query declared via [Query] annotation followed by
     * a JPA named query lookup.
     *
     * @author Oliver Gierke
     * @author Thomas Darimont
     * @author Jens Schauder
     */
    private class DeclaredQueryLookupStrategy(
        em: EntityManager,
        queryMethodFactory: JpaQueryMethodFactory,
        private val evaluationContextProvider: QueryMethodEvaluationContextProvider,
        queryRewriterProvider: QueryRewriterProvider
    ) : AbstractQueryLookupStrategy(em, queryMethodFactory, queryRewriterProvider) {
        public override fun resolveQuery(
            method: JpaQueryMethod,
            queryRewriter: QueryRewriter,
            em: EntityManager,
            namedQueries: NamedQueries
        ): RepositoryQuery {
            if (method.isProcedureQuery) {
                return JpaQueryFactory.INSTANCE.fromProcedureAnnotation(method, em)
            }

            if (StringUtils.hasText(method.annotatedQuery)) {
                if (method.hasAnnotatedQueryName()) {
                    logger.warn {
                        "Query method $method is annotated with both, a query and a query name; Using the declared query"
                    }
                }

                return JpaQueryFactory.INSTANCE.fromMethodWithQueryString(
                    method, em, method.requiredAnnotatedQuery,
                    getCountQuery(method, namedQueries, em), queryRewriter, evaluationContextProvider
                )
            }

            val name = method.namedQueryName
            if (namedQueries.hasQuery(name)) {
                return JpaQueryFactory.INSTANCE.fromMethodWithQueryString(
                    method, em, namedQueries.getQuery(name),
                    getCountQuery(method, namedQueries, em), queryRewriter, evaluationContextProvider
                )
            }

            val query = NamedQuery.lookupFrom(method, em)

            return query ?: NO_QUERY
        }

        @Nullable
        fun getCountQuery(method: JpaQueryMethod, namedQueries: NamedQueries, em: EntityManager): String? {
            if (StringUtils.hasText(method.countQuery)) {
                return method.countQuery
            }

            val queryName = method.getNamedCountQueryName()

            if (!StringUtils.hasText(queryName)) {
                return method.countQuery
            }

            if (namedQueries.hasQuery(queryName)) {
                return namedQueries.getQuery(queryName)
            }

            val namedQuery = NamedQuery.hasNamedQuery(em, queryName)

            if (namedQuery) {
                return method.queryExtractor.extractQueryString(em.createNamedQuery(queryName))
            }

            return null
        }
    }

    /**
     * [QueryLookupStrategy] to try to detect a declared query first (
     * [org.springframework.data.jpa.repository.Query], JPA named query). In case none is found we fall back on
     * query creation.
     *
     * @author Oliver Gierke
     * @author Thomas Darimont
     */
    private class CreateIfNotFoundQueryLookupStrategy(
        em: EntityManager,
        queryMethodFactory: JpaQueryMethodFactory,
        private val createStrategy: CreateQueryLookupStrategy,
        private val lookupStrategy: DeclaredQueryLookupStrategy,
        queryRewriterProvider: QueryRewriterProvider,
    ) : AbstractQueryLookupStrategy(em, queryMethodFactory, queryRewriterProvider) {

        override fun resolveQuery(
            method: JpaQueryMethod,
            queryRewriter: QueryRewriter,
            em: EntityManager,
            namedQueries: NamedQueries,
        ): RepositoryQuery {
            val lookupQuery = lookupStrategy.resolveQuery(method, queryRewriter, em, namedQueries)

            if (lookupQuery !== NO_QUERY) {
                return lookupQuery
            }

            return createStrategy.resolveQuery(method, queryRewriter, em, namedQueries)
        }
    }

    /**
     * A null value type that represents the lack of a defined query.
     */
    internal class NoQuery : RepositoryQuery {
        override fun execute(parameters: Array<Any>): Any {
            throw IllegalStateException("NoQuery should not be executed!")
        }

        override fun getQueryMethod(): QueryMethod {
            throw IllegalStateException("NoQuery does not have a QueryMethod!")
        }
    }
}