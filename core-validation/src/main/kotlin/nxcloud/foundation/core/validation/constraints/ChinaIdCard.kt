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
    val message: String = "身份证号格式不正确或年龄不满足",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
    // 增加最小和最大年龄参数,默认值-1表示不检查
    val ageMin: Int = -1,
    val ageMax: Int = -1,
)

class ChinaIdCardValidator : ConstraintValidator<ChinaIdCard, String> {
    private var ageMin: Int = -1
    private var ageMax: Int = -1

    override fun initialize(constraintAnnotation: ChinaIdCard) {
        this.ageMin = constraintAnnotation.ageMin
        this.ageMax = constraintAnnotation.ageMax
    }

    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        return value
            ?.let {
                ChinaIdCardHelper.isValidIdCard(it, this.ageMin, this.ageMax)
            }
            ?: true
    }
}