package nxcloud.foundation.core.data.jpa.id

import mu.KotlinLogging
import nxcloud.foundation.core.idgenerator.IdGeneratorFacade
import nxcloud.foundation.core.spring.support.SpringContextHelper
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

    private val identifierGenerator: IdGeneratorFacade<Long> by lazy {
        SpringContextHelper.getBean(
            ResolvableType.forClassWithGenerics(
                IdGeneratorFacade::class.java,
                Long::class.javaObjectType
            )
        )
    }

    override fun generate(session: SharedSessionContractImplementor, obj: Any): Serializable {
        logger.debug { "generate id: $obj" }

//        val generatorFactory = serviceRegistry.getService(MutableIdentifierGeneratorFactory::class.java)
//
//        val generator = generatorFactory.createIdentifierGenerator("identity", type, params)
//        generator.configure(type, params, serviceRegistry)
//
//        return generator.generate(session, obj)

        return identifierGenerator.nextId()
    }

    override fun configure(type: Type, params: Properties, serviceRegistry: ServiceRegistry) {
        logger.debug { "configure: type=$type, params=$params, serviceRegistry=$serviceRegistry" }
        this.type = type
        this.params = params
        this.serviceRegistry = serviceRegistry
    }

}