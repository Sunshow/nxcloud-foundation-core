package nxcloud.foundation.core.data.support.listener

interface EntityLifecycleListener {

    fun onPrePersist(entity: Any) {

    }

    fun onPostPersist(entity: Any) {

    }

    fun onPreUpdate(entity: Any) {

    }

    fun onPostUpdate(entity: Any) {

    }

    fun onPreRemove(entity: Any) {

    }

    fun onPostRemove(entity: Any) {

    }

    fun onPostLoad(entity: Any) {

    }

}