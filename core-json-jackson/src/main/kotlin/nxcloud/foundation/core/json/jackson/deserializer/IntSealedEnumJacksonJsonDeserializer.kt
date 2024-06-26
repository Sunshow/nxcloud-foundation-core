package nxcloud.foundation.core.json.jackson.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import nxcloud.foundation.core.lang.enumeration.IntSealedEnum

class IntSealedEnumJacksonJsonDeserializer<T : IntSealedEnum>(
    private val enumType: Class<T>
) : JsonDeserializer<T>() {

    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext
    ): T {
        val value = p.intValue
        return IntSealedEnum.valueOf(enumType, value)
            ?: throw IllegalArgumentException("Unknown value of $enumType: $value")
    }

}