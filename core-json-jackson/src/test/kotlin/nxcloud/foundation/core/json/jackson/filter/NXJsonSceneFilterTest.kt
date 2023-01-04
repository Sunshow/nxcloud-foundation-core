package nxcloud.foundation.core.json.jackson.filter

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier
import com.fasterxml.jackson.databind.ser.ContextualSerializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import nxcloud.foundation.core.json.annotation.NXJsonSceneFilter
import org.junit.jupiter.api.Test
import java.io.StringWriter

class NXJsonSceneFilterTest {

    @Test
    fun testSceneFilter() {
        val employee = Employee("张三", 18, listOf("篮球", "足球", "乒乓球"))
        val objectMapper = ObjectMapper()
        objectMapper.registerModule(object : SimpleModule() {
            override fun setupModule(context: SetupContext) {
                super.setupModule(context)

                context.addBeanSerializerModifier(object : BeanSerializerModifier() {
                    override fun modifySerializer(
                        config: SerializationConfig,
                        beanDesc: BeanDescription,
                        serializer: JsonSerializer<*>
                    ): JsonSerializer<*> {
                        return SceneJsonSerializer(config, beanDesc, serializer)
                    }
                })

            }
        })

        val writer = SceneStringWriter("test")
        objectMapper.writeValue(writer, employee)

        println(writer.toString())
    }

    internal class SceneStringWriter(val scene: String) : StringWriter()

    internal class SceneJsonSerializer<T>(
        private val config: SerializationConfig,
        private val beanDesc: BeanDescription,
        private val defaultSerializer: JsonSerializer<T>,
    ) : StdSerializer<T>(beanDesc.type),
        ContextualSerializer {

        override fun serialize(value: T, gen: JsonGenerator, serializers: SerializerProvider) {
            val outputTarget = gen.outputTarget
            if (outputTarget is SceneStringWriter) {
                val scene = outputTarget.scene
            }
            return defaultSerializer.serialize(value, gen, serializers)
        }

        override fun createContextual(prov: SerializerProvider, property: BeanProperty?): JsonSerializer<*> {
            println(property)
            println(beanDesc.classAnnotations.get(NXJsonSceneFilter::class.java))
            println(property?.getAnnotation(NXJsonSceneFilter::class.java))
            return this
        }
    }
}