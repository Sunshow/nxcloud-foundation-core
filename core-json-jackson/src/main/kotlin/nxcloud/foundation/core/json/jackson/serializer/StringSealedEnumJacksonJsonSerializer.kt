package nxcloud.foundation.core.json.jackson.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import nxcloud.foundation.core.lang.enumeration.StringSealedEnum

open class StringSealedEnumJacksonJsonSerializer : JsonSerializer<StringSealedEnum>() {

    override fun serialize(value: StringSealedEnum, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(value.value)
    }

}