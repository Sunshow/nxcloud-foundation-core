package nxcloud.foundation.core.data.jpa.id

import io.github.oshai.kotlinlogging.KotlinLogging
import nxcloud.foundation.core.idgenerator.IdGeneratorFacade
import nxcloud.foundation.core.spring.support.SpringContextHelper
import org.hibernate.MappingException
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.IdentifierGenerator
import org.hibernate.service.ServiceRegistry
import org.hibernate.type.Type
import org.springframework.core.ResolvableType
import java.io.Serializable
import java.util.*

class DeployContextIdentifierGenerator : IdentifierGenerator {

    private val logger = KotlinLogging.logger {}

    private lateinit var type: Type

    private lateinit var params: Properties

    private lateinit var serviceRegistry: ServiceRegistry

    private lateinit var entityName: String

    private val identifierGenerator: IdGeneratorFacade<Long> by lazy {
        SpringContextHelper.getBean(
            ResolvableType.forClassWithGenerics(
                IdGeneratorFacade::class.java,
                Long::class.javaObjectType
            )
        )
    }

    override fun generate(session: SharedSessionContractImplementor, obj: Any): Serializable {
        logger.debug { "generate id for $obj" }

//        val generatorFactory = serviceRegistry.getService(MutableIdentifierGeneratorFactory::class.java)
//
//        val generator = generatorFactory.createIdentifierGenerator("identity", type, params)
//        generator.configure(type, params, serviceRegistry)
//
//        return generator.generate(session, obj)

        val assignedId = session.getEntityPersister(entityName, obj).getIdentifier(obj, session)
        if (assignedId != null && assignedId.toString().toLong() > 0) {
            logger.debug { "already assigned id: $assignedId, won't generate" }
            return assignedId as Serializable
        }

        val next = identifierGenerator.nextId()

        // logger out generated id
        logger.debug { "generate id is $next" }

        return next
    }

    override fun configure(type: Type, params: Properties, serviceRegistry: ServiceRegistry) {
        logger.debug { "configure: type=$type, params=$params, serviceRegistry=$serviceRegistry" }
        this.type = type
        this.params = params
        this.serviceRegistry = serviceRegistry

        entityName = params.getProperty(IdentifierGenerator.ENTITY_NAME) ?: throw MappingException("no entity name")
    }

}