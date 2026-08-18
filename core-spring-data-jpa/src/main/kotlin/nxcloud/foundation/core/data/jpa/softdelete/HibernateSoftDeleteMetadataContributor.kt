package nxcloud.foundation.core.data.jpa.softdelete

import nxcloud.foundation.core.data.jpa.constant.JpaConstants
import nxcloud.foundation.core.data.support.annotation.EnableSoftDelete
import org.hibernate.AnnotationException
import org.hibernate.MappingException
import org.hibernate.boot.ResourceStreamLocator
import org.hibernate.boot.spi.AdditionalMappingContributions
import org.hibernate.boot.spi.AdditionalMappingContributor
import org.hibernate.boot.spi.InFlightMetadataCollector
import org.hibernate.boot.spi.MetadataBuildingContext
import org.hibernate.engine.spi.FilterDefinition
import org.hibernate.mapping.PersistentClass
import org.hibernate.type.StandardBasicTypes
import org.springframework.core.annotation.AnnotationUtils

/**
 * Installs soft-delete filters on ORM mappings marked with [EnableSoftDelete].
 * Native SQL bypasses Hibernate filters and must include its own deleted predicate.
 */
class HibernateSoftDeleteMetadataContributor : AdditionalMappingContributor {

    override fun getContributorName(): String = "nxcloud-soft-delete"

    override fun contribute(
        contributions: AdditionalMappingContributions,
        metadata: InFlightMetadataCollector,
        resourceStreamLocator: ResourceStreamLocator,
        buildingContext: MetadataBuildingContext,
    ) {
        registerFilterDefinitions(metadata)

        metadata.entityBindings
            .filter(::isSoftDeleteEntity)
            .forEach(::applySoftDeleteFilters)
    }

    private fun registerFilterDefinitions(metadata: InFlightMetadataCollector) {
        val longType = metadata.typeConfiguration.basicTypeRegistry.resolve(StandardBasicTypes.LONG)

        addFilterDefinition(
            metadata,
            FilterDefinition(
                JpaConstants.FILTER_NOT_DELETED,
                NOT_DELETED_CONDITION,
                emptyMap(),
                emptyMap(),
                true,
                true,
            )
        )
        addFilterDefinition(
            metadata,
            FilterDefinition(
                JpaConstants.FILTER_DELETED_AFTER,
                DELETED_AFTER_CONDITION,
                mapOf(JpaConstants.FILTER_PARAMETER_DELETED_AFTER to longType),
                emptyMap(),
                false,
                true,
            )
        )
        addFilterDefinition(
            metadata,
            FilterDefinition(
                JpaConstants.FILTER_DELETED_BEFORE,
                DELETED_BEFORE_CONDITION,
                mapOf(JpaConstants.FILTER_PARAMETER_DELETED_BEFORE to longType),
                emptyMap(),
                false,
                true,
            )
        )
    }

    private fun addFilterDefinition(
        metadata: InFlightMetadataCollector,
        definition: FilterDefinition,
    ) {
        val name = definition.filterName
        if (metadata.getFilterDefinition(name) != null) {
            throw AnnotationException("Filter '$name' is reserved by NXCloud soft-delete support")
        }
        metadata.addFilterDefinition(definition)
    }

    private fun isSoftDeleteEntity(entity: PersistentClass): Boolean {
        return AnnotationUtils.findAnnotation(entity.mappedClass, EnableSoftDelete::class.java) != null
    }

    private fun applySoftDeleteFilters(entity: PersistentClass) {
        try {
            entity.getProperty(DELETED_PROPERTY)
        } catch (exception: MappingException) {
            throw AnnotationException(
                "Entity '${entity.entityName}' is annotated with @EnableSoftDelete but has no '$DELETED_PROPERTY' property",
                exception,
            )
        }

        entity.addFilter(
            JpaConstants.FILTER_NOT_DELETED,
            NOT_DELETED_CONDITION,
            true,
            emptyMap(),
            emptyMap(),
        )
        entity.addFilter(
            JpaConstants.FILTER_DELETED_AFTER,
            DELETED_AFTER_CONDITION,
            true,
            emptyMap(),
            emptyMap(),
        )
        entity.addFilter(
            JpaConstants.FILTER_DELETED_BEFORE,
            DELETED_BEFORE_CONDITION,
            true,
            emptyMap(),
            emptyMap(),
        )
    }

    private companion object {
        const val DELETED_PROPERTY = "deleted"
        const val NOT_DELETED_CONDITION = "$DELETED_PROPERTY = 0"
        const val DELETED_AFTER_CONDITION =
            "$DELETED_PROPERTY > :${JpaConstants.FILTER_PARAMETER_DELETED_AFTER}"
        const val DELETED_BEFORE_CONDITION =
            "$DELETED_PROPERTY < :${JpaConstants.FILTER_PARAMETER_DELETED_BEFORE}"
    }
}
