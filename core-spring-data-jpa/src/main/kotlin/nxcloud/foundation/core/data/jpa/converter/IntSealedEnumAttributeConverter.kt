package nxcloud.foundation.core.data.jpa.converter

import nxcloud.foundation.core.lang.enumeration.IntSealedEnum
import javax.persistence.AttributeConverter
import kotlin.reflect.KClass

abstract class NullableIntSealedEnumAttributeConverter<T : IntSealedEnum> : AttributeConverter<T, Int> {

    override fun convertToDatabaseColumn(attribute: T?): Int? {
        return attribute?.value
    }

    override fun convertToEntityAttribute(dbData: Int?): T? {
        return dbData?.let { data ->
            @Suppress("UNCHECKED_CAST")
            val kClass =
                this::class.supertypes.first { it.classifier == NullableIntSealedEnumAttributeConverter::class }.arguments.first().type!!.classifier as KClass<T>
            IntSealedEnum.valueOf(kClass.java, data)
        }
    }

}

abstract class NonnullIntSealedEnumAttributeConverter<T : IntSealedEnum> : AttributeConverter<T, Int> {

    override fun convertToDatabaseColumn(attribute: T?): Int {
        return attribute?.value ?: convertToDatabaseColumnIfNull()
    }

    protected abstract fun convertToDatabaseColumnIfNull(): Int

    override fun convertToEntityAttribute(dbData: Int?): T {
        return dbData?.let { data ->
            @Suppress("UNCHECKED_CAST")
            val kClass =
                this::class.supertypes.first { it.classifier == NonnullIntSealedEnumAttributeConverter::class }.arguments.first().type!!.classifier as KClass<T>
            IntSealedEnum.valueOf(kClass.java, data)
        } ?: convertToEntityAttributeIfNull()
    }

    protected abstract fun convertToEntityAttributeIfNull(): T

}