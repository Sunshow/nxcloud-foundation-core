package nxcloud.foundation.core.lang.enumeration

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
}

sealed class YesNoStatus(
    value: Int,
    name: String,
) : IntSealedEnum(value, name) {
    object Yes : YesNoStatus(1, "是")
    object No : YesNoStatus(0, "否")
}