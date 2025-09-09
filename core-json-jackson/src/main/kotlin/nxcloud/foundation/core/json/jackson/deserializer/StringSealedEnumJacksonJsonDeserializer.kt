package nxcloud.foundation.core.json.jackson.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import io.github.oshai.kotlinlogging.KotlinLogging
import nxcloud.foundation.core.lang.enumeration.StringSealedEnum

open class StringSealedEnumJacksonJsonDeserializer<T : StringSealedEnum>(
    private val enumType: Class<T>
) : JsonDeserializer<T>() {

    private val logger = KotlinLogging.logger {}

    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext
    ): T? {
        val value: String = p.valueAsString

        return value
            .let {
                StringSealedEnum.valueOf(enumType, value)
            }
    }

}