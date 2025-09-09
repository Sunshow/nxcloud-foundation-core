package nxcloud.foundation.core.data.jpa.converter

import jakarta.persistence.AttributeConverter
import nxcloud.foundation.core.lang.enumeration.StringSealedEnum
import nxcloud.foundation.core.lang.exception.SealedEnumUnrecognizedException
import kotlin.reflect.KClass

abstract class NullableStringSealedEnumAttributeConverter<T : StringSealedEnum> : AttributeConverter<T, String> {

    override fun convertToDatabaseColumn(attribute: T?): String? {
        return attribute?.value
    }

    override fun convertToEntityAttribute(dbData: String?): T? {
        return dbData?.let { data ->
            @Suppress("UNCHECKED_CAST")
            val kClass =
                this::class.supertypes.first { it.classifier == NullableStringSealedEnumAttributeConverter::class }.arguments.first().type!!.classifier as KClass<T>
            StringSealedEnum.valueOf(kClass.java, data)
        }
    }

}

abstract class ConvertIfNullStringSealedEnumAttributeConverter<T : StringSealedEnum> : AttributeConverter<T, String> {

    override fun convertToDatabaseColumn(attribute: T?): String {
        return attribute?.value ?: convertToDatabaseColumnIfNull()
    }

    protected abstract fun convertToDatabaseColumnIfNull(): String

    override fun convertToEntityAttribute(dbData: String?): T {
        return dbData?.let { data ->
            @Suppress("UNCHECKED_CAST")
            val kClass =
                this::class.supertypes.first { it.classifier == ConvertIfNullStringSealedEnumAttributeConverter::class }.arguments.first().type!!.classifier as KClass<T>
            StringSealedEnum.valueOf(kClass.java, data)
        } ?: convertToEntityAttributeIfNull()
    }

    protected abstract fun convertToEntityAttributeIfNull(): T

}

abstract class NonnullStringSealedEnumAttributeConverter<T : StringSealedEnum> : AttributeConverter<T, String> {

    override fun convertToDatabaseColumn(attribute: T): String {
        return attribute.value
    }

    override fun convertToEntityAttribute(dbData: String): T {
        return dbData.let { data ->
            @Suppress("UNCHECKED_CAST")
            val kClass =
                this::class.supertypes.first { it.classifier == NonnullStringSealedEnumAttributeConverter::class }.arguments.first().type!!.classifier as KClass<T>
            StringSealedEnum.valueOf(kClass.java, data)
        } ?: throw SealedEnumUnrecognizedException("Unrecognized sealed enum value: $dbData")
    }

}