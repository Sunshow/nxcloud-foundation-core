package nxcloud.foundation.core.json.jackson.serializer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import nxcloud.foundation.core.lang.enumeration.YesNoStatus
import kotlin.test.Test

class IntSealedEnumJacksonJsonSerializerTest {

    private val objectMapper = ObjectMapper()
        .apply {
            // 注册序列化
            registerModules(object : SimpleModule() {
                init {
                    addSerializer(YesNoStatus::class.java, IntSealedEnumJacksonJsonSerializer())
                }
            })
        }

    @Test
    fun testSerialize() {
        println(objectMapper.writeValueAsString(YesNoStatus.Yes))
        println(objectMapper.writeValueAsString(YesNoStatus.No))
    }

}