package nxcloud.foundation.core.json.jackson.deserializer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import nxcloud.foundation.core.lang.enumeration.YesNoStatus
import kotlin.test.Test

class IntSealedEnumJacksonJsonDeserializerTest {

    private val objectMapper = ObjectMapper()
        .apply {
            // 注册序列化
            registerModules(object : SimpleModule() {
                init {
                    addDeserializer(
                        YesNoStatus::class.java,
                        IntSealedEnumJacksonJsonDeserializer(YesNoStatus::class.java)
                    )
                }
            })
        }

    @Test
    fun testDeserialize() {
        println(YesNoStatus.Yes == objectMapper.readValue("1", YesNoStatus::class.java))
        println(YesNoStatus.No == objectMapper.readValue("0", YesNoStatus::class.java))
    }

}