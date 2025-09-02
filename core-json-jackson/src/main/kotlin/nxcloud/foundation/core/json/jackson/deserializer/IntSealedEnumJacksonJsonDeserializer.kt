package nxcloud.foundation.core.json.jackson.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import io.github.oshai.kotlinlogging.KotlinLogging
import nxcloud.foundation.core.lang.enumeration.IntSealedEnum

open class IntSealedEnumJacksonJsonDeserializer<T : IntSealedEnum>(
    private val enumType: Class<T>
) : JsonDeserializer<T>() {

    private val logger = KotlinLogging.logger {}

    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext
    ): T? {
        val currentToken = p.currentToken
        var value: Int? = null
        if (currentToken == JsonToken.VALUE_NUMBER_INT) {
            value = p.intValue
        } else if (currentToken == JsonToken.VALUE_STRING) {
            val s = p.valueAsString
            try {
                value = s.toInt()
            } catch (e: NumberFormatException) {
                logger.error(e) {
                    ("解析自定义enum出错, 无效的值: $s")
                }
            }
        }

        return value
            ?.let {
                IntSealedEnum.valueOf(enumType, value)
            }
    }

}