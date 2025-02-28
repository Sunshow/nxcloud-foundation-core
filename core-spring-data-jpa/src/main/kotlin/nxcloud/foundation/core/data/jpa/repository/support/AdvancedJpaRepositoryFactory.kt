package nxcloud.foundation.core.data.jpa.repository.support

import jakarta.persistence.EntityManager
import org.springframework.data.jpa.repository.query.EscapeCharacter
import org.springframework.data.jpa.repository.query.JpaQueryMethodFactory
import org.springframework.data.jpa.repository.query.QueryRewriterProvider
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory
import org.springframework.data.repository.core.RepositoryMetadata

open class AdvancedJpaRepositoryFactory(
    em: EntityManager,
) : JpaRepositoryFactory(em) {

    private val entityManager by lazy {
        acquireParentPrivateProperty<EntityManager>("entityManager")
    }

    private val queryMethodFactory by lazy {
        acquireParentPrivateProperty<JpaQueryMethodFactory>("queryMethodFactory")
    }

    private val queryRewriterProvider by lazy {
        acquireParentPrivateProperty<QueryRewriterProvider>("queryRewriterProvider")
    }

    private val escapeCharacter by lazy {
        acquireParentPrivateProperty<EscapeCharacter>("escapeCharacter")
    }

    /**
     * 反射获取父类和祖先类的属性
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> acquireParentPrivateProperty(propertyName: String): T {
        var clazz: Class<*>? = this::class.java
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(propertyName)
                    .apply {
                        trySetAccessible()
                    }
                    .get(this) as T
            } catch (e: NoSuchFieldException) {
                clazz = clazz.superclass
            }
        }
        throw NoSuchFieldException("Field $propertyName not found in class hierarchy")
    }

    override fun getRepositoryBaseClass(metadata: RepositoryMetadata): Class<*> {
        return AdvancedJpaRepository::class.java
    }
}