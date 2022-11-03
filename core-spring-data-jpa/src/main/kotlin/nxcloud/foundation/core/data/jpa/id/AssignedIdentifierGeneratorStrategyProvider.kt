package nxcloud.foundation.core.data.jpa.id

import nxcloud.foundation.core.data.jpa.constant.JpaConstants
import org.hibernate.id.Assigned
import org.hibernate.jpa.spi.IdentifierGeneratorStrategyProvider

class AssignedIdentifierGeneratorStrategyProvider : IdentifierGeneratorStrategyProvider {
    override fun getStrategies(): Map<String, Class<*>> {
        return mapOf(JpaConstants.ID_GENERATOR_STRATEGY to Assigned::class.java)
    }
}