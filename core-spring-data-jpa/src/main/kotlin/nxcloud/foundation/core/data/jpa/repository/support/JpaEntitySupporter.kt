package nxcloud.foundation.core.data.jpa.repository.support

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.Table
import nxcloud.foundation.core.data.jpa.entity.JpaEntityMetadata
import nxcloud.foundation.core.data.support.annotation.EnableSoftDelete
import org.springframework.beans.factory.InitializingBean
import org.springframework.core.annotation.AnnotationUtils

class JpaEntitySupporter : InitializingBean {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    private lateinit var tableNameMetadataMap: Map<String, JpaEntityMetadata>

    private lateinit var entityMetadataMap: Map<Class<*>, JpaEntityMetadata>

    private fun convertJpaEntityMetadata(entityType: Class<*>): JpaEntityMetadata {
        // TODO Hibernate6 之后无法获取到真实表名, 暂时先用 @Table 声明的逻辑表名满足项目使用
        val table = AnnotationUtils.findAnnotation(entityType, Table::class.java)
        return JpaEntityMetadata(
            entityType = entityType,
            tableName = table?.name ?: "",
            enableSoftDelete = AnnotationUtils.findAnnotation(entityType, EnableSoftDelete::class.java) != null,
        )
    }

    override fun afterPropertiesSet() {
        val metadataList = entityManager.metamodel.entities
            .map { it.javaType }
            .map {
                convertJpaEntityMetadata(it)
            }

        tableNameMetadataMap = metadataList
            .filter {
                it.tableName.isNotBlank()
            }
            .associateBy {
                it.tableName
            }

        entityMetadataMap = metadataList
            .associateBy {
                it.entityType
            }
    }

    fun getMetadataByPhysicalTableName(tableName: String): JpaEntityMetadata? {
        return tableNameMetadataMap[tableName]
    }

    fun getMetadataByEntityType(entityType: Class<*>): JpaEntityMetadata? {
        return entityMetadataMap[entityType]
    }

}