package nxcloud.foundation.core.lang.enumeration

import java.io.InvalidObjectException
import java.io.ObjectStreamException
import java.io.Serializable

sealed class SealedEnum : Serializable

abstract class IntSealedEnum(
    val value: Int,
    val name: String,
) : SealedEnum() {
    companion object {
        @JvmStatic
        fun <T : IntSealedEnum> all(javaClass: Class<T>): List<T> {
            return javaClass.kotlin.sealedSubclasses
                .map {
                    it.objectInstance as T
                }
        }

        @JvmStatic
        fun <T : IntSealedEnum> valueOf(javaClass: Class<T>, value: Int): T? {
            return all(javaClass)
                .firstOrNull {
                    it.value == value
                }
        }

        inline fun <reified T : SealedEnum> all(): List<T> {
            return T::class.sealedSubclasses
                .map {
                    it.objectInstance as T
                }
        }

        inline fun <reified T : IntSealedEnum> valueOf(value: Int): T? {
            return all<T>()
                .firstOrNull {
                    it.value == value
                }
        }
    }

    @Throws(ObjectStreamException::class)
    protected fun readResolve(): Any {
        // Use reflection to find the instance of the current class
        val clazz = this::class
        return clazz.objectInstance ?: throw InvalidObjectException("Cannot resolve object instance for $clazz")
    }
}

sealed class YesNoStatus(
    value: Int,
    name: String,
) : IntSealedEnum(value, name) {
    data object Yes : YesNoStatus(1, "是")
    data object No : YesNoStatus(0, "否")
}