package nxcloud.foundation.core.idgenerator.impl.snowflake

import nxcloud.foundation.core.idgenerator.IdGeneratorFacade

class SnowFlakeIdGeneratorFacadeImpl(
    private val generator: SnowFlakeIdGenerator = SnowFlakeIdGenerator(),
) : IdGeneratorFacade<Long> {
    override fun nextId(): Long {
        return generator.next()
    }
}