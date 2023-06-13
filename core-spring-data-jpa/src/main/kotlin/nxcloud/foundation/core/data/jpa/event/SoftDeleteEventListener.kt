package nxcloud.foundation.core.data.jpa.event

import mu.KotlinLogging
import nxcloud.foundation.core.data.jpa.entity.DeletedField
import org.hibernate.event.spi.PreDeleteEvent
import org.hibernate.event.spi.PreDeleteEventListener

class SoftDeleteEventListener : PreDeleteEventListener {

    private val logger = KotlinLogging.logger {}

    override fun onPreDelete(event: PreDeleteEvent): Boolean {
        val entity = event.entity

        if (entity is DeletedField) {
            // 禁用物理删除
            logger.error { "当前实体已启用软删除, 禁用物理删除, entity=$entity" }
            return true
        }

        return false
    }
}