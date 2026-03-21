package nxcloud.foundation.core.normalizer

import nxcloud.foundation.core.normalizer.annotation.FieldNormalizer
import nxcloud.foundation.core.normalizer.annotation.NormalizeMarker
import java.lang.reflect.Field
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

object NormalizeExtensions {

    private val fieldCache = ConcurrentHashMap<Class<*>, List<NormalizeFieldMeta>>()

    private val handlerCache = ConcurrentHashMap<KClass<*>, FieldNormalizer<Annotation>>()

    private class NormalizeFieldMeta(
        val field: Field,
        val annotation: Annotation,
        val handlerClass: KClass<out FieldNormalizer<*>>,
    )

    private fun resolve(clazz: Class<*>): List<NormalizeFieldMeta> {
        return fieldCache.computeIfAbsent(clazz) {
            clazz.declaredFields.flatMap { field ->
                field.annotations.mapNotNull { annotation ->
                    val marker = annotation.annotationClass
                        .java.getAnnotation(NormalizeMarker::class.java)
                        ?: return@mapNotNull null
                    field.isAccessible = true
                    NormalizeFieldMeta(field, annotation, marker.handler)
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getHandler(clazz: KClass<out FieldNormalizer<*>>): FieldNormalizer<Annotation> {
        return handlerCache.computeIfAbsent(clazz) {
            it.java.getDeclaredConstructor().newInstance() as FieldNormalizer<Annotation>
        }
    }

    fun <T : Any> T.normalizeFields(): T {
        for (meta in resolve(this::class.java)) {
            val value = meta.field.get(this) ?: continue
            val handler = getHandler(meta.handlerClass)
            val normalized = handler.normalize(meta.annotation, value)
            meta.field.set(this, normalized)
        }
        return this
    }
}
