package nxcloud.foundation.core.json.jackson.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import nxcloud.foundation.core.lang.enumeration.IntSealedEnum

class IntSealedEnumJacksonJsonSerializer : JsonSerializer<IntSealedEnum>() {

    override fun serialize(value: IntSealedEnum, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeNumber(value.value)
    }

}