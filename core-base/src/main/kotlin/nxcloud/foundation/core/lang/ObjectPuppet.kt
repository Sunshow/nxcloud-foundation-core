package nxcloud.foundation.core.lang

open class ObjectPuppet<T>(val value: T?) {

    companion object {
        fun <T> of(value: T?): ObjectPuppet<T> {
            return ObjectPuppet(value)
        }
    }

}

data object NullPuppet : ObjectPuppet<Any>(null)