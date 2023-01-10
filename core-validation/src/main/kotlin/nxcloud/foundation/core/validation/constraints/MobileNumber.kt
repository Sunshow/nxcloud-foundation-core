package nxcloud.foundation.core.validation.constraints

import nxcloud.foundation.core.util.PhoneNumberHelper
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

/**
 * 11位手机号
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [MobileNumberValidator::class])
annotation class MobileNumber(
    val message: String = "手机号格式不正确",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class MobileNumberValidator : ConstraintValidator<MobileNumber, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        return value
            ?.let {
                PhoneNumberHelper.isValidMobile(it)
            }
            ?: true
    }
}