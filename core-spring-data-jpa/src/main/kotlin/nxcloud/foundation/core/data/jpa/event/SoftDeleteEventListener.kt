package nxcloud.foundation.core.data.jpa.event

import nxcloud.foundation.core.data.jpa.entity.SoftDeleteJpaEntity
import org.hibernate.event.spi.PreDeleteEvent
import org.hibernate.event.spi.PreDeleteEventListener

class SoftDeleteEventListener : PreDeleteEventListener {
    override fun onPreDelete(event: PreDeleteEvent): Boolean {
        val entity = event.entity

        if (entity is SoftDeleteJpaEntity) {
            //entity.deleted = System.currentTimeMillis()
            // event.session.update(entity)
//            event.session.flush()
            // 禁用物理删除
            return true
        }

        return false
    }
}