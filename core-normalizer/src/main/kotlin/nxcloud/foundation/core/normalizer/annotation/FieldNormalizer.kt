package nxcloud.foundation.core.normalizer.annotation

interface FieldNormalizer<A : Annotation> {
    fun normalize(annotation: A, value: Any): Any?
}
