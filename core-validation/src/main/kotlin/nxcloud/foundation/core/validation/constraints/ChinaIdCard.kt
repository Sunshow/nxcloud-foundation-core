package nxcloud.foundation.core.validation.constraints

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import nxcloud.foundation.core.util.ChinaIdCardHelper
import kotlin.reflect.KClass

/**
 * 中国身份证号
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [ChinaIdCardValidator::class])
annotation class ChinaIdCard(
    val message: String = "身份证号格式不正确",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class ChinaIdCardValidator : ConstraintValidator<ChinaIdCard, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        return value
            ?.let {
                ChinaIdCardHelper.isValidIdCard(it)
            }
            ?: true
    }
}