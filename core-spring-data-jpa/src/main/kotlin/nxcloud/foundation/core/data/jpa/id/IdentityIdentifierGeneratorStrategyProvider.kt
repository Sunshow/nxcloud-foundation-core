package nxcloud.foundation.core.data.jpa.id

import nxcloud.foundation.core.data.jpa.constant.JpaConstants
import org.hibernate.id.IdentityGenerator
import org.hibernate.jpa.spi.IdentifierGeneratorStrategyProvider

class IdentityIdentifierGeneratorStrategyProvider : IdentifierGeneratorStrategyProvider {
    override fun getStrategies(): Map<String, Class<*>> {
        return mapOf(JpaConstants.ID_GENERATOR_STRATEGY to IdentityGenerator::class.java)
    }
}