package nxcloud.foundation.core.util

import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.max

/**
 * 身份证校验工具类
 */
object ChinaIdCardHelper {

    private val logger = KotlinLogging.logger {}

    /**
     * 验证位码表
     */
    private val CheckCodes = arrayOf("1", "0", "x", "9", "8", "7", "6", "5", "4", "3", "2")
    private val W = arrayOf("7", "9", "10", "5", "8", "4", "2", "1", "6", "3", "7", "9", "10", "5", "8", "4", "2")

    private val yyyyMMdd: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    private val ProvinceCodes = mapOf(
        "11" to "北京",
        "12" to "天津",
        "13" to "河北",
        "14" to "山西",
        "15" to "内蒙古",
        "21" to "辽宁",
        "22" to "吉林",
        "23" to "黑龙江",
        "31" to "上海",
        "32" to "江苏",
        "33" to "浙江",
        "34" to "安徽",
        "35" to "福建",
        "36" to "江西",
        "37" to "山东",
        "41" to "河南",
        "42" to "湖北",
        "43" to "湖南",
        "44" to "广东",
        "45" to "广西",
        "46" to "海南",
        "50" to "重庆",
        "51" to "四川",
        "52" to "贵州",
        "53" to "云南",
        "54" to "西藏",
        "61" to "陕西",
        "62" to "甘肃",
        "63" to "青海",
        "64" to "宁夏",
        "65" to "新疆",
        "71" to "台湾",
        "81" to "香港",
        "82" to "澳门",
        "91" to "国外"
    )

    /*
    校验规则：
        如果为15位，只能是15位数字；前两位满足省/直辖市的行政区划代码。
        如果为18位，允许为18位数字，如出现字母只能在最后一位，且仅能为“X”；
        18位中包含年月的字段满足日期的构成规则；前两位满足省/直辖市的行政区划代码；
        最后一位校验位满足身份证的校验规则（身份证校验规则见附录）。
        附录：身份证校验规则
            公民身份证号码校验公式为RESULT = ∑( A[i] * W[i] ) mod 11。
            其中,i表示号码字符从右至左包括校验码在内的位置序号;A[i]表示第I位置上的数字的数值;W[i]表示第i位置上的加权因子,其值如下:

            i 18 17 16 15 14 13 12 11 10 9 8 7 6 5 4 3 2
            W[i] 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 2

            RESULT 0 1 2 3 4 5 6 7 8 9 10
            校验码A[1] 1 0 X 9 8 7 6 5 4 3 2
     */
    fun isValidIdCard(idCard: String, ageMin: Int = -1, ageMax: Int = -1): Boolean {
        val idCardObj = tryParse(idCard) ?: return false

        // 判断出生日期是否有效, 在当前时间之前且距离当前150年以内
        val age = idCardObj.age
        if (age > max(150, ageMax)) {
            logger.error { "身份证号码格式错误，年龄超出上限" }
            return false
        }

        if (ageMin >= 0 && age < ageMin) {
            logger.error { "身份证号码格式错误，年龄小于最小年龄" }
            return false
        }

        return true
    }

    /**
     * 解析身份证号码
     */
    fun tryParse(idCard: String): IdCard? {
        // 号码的长度 18位
        if (idCard.length != 18) {
            return null
        }

        // 数字 除最后一位都为数字
        val ai = idCard.substring(0, 17)
            .takeIf {
                it.all { c ->
                    c.isDigit()
                }
            }
            ?: run {
                logger.error { "身份证号码格式错误，前17位必须为数字" }

                return null
            }

        // 最后一位必须是数字或者 X
        val checkCode = idCard
            .substring(17)
            .uppercase(Locale.getDefault())
            .takeIf {
                it.all { c ->
                    c.isDigit() || c == 'X'
                }
            }
            ?: run {
                logger.error { "身份证号码格式错误，最后一位必须为数字或者 X" }

                return null
            }

        // 地区码是否有效
        val areaCode = ai.substring(0, 6)
        val provinceCode = areaCode.substring(0, 2)
        if (ProvinceCodes.containsKey(provinceCode).not()) {
            logger.error { "身份证号码格式错误，前两位不是有效的地区码" }
            return null
        }

        // 出生年月是否有效
        val strYear = ai.substring(6, 10) // 年份
        val strMonth = ai.substring(10, 12) // 月份
        val strDay = ai.substring(12, 14) // 月份

        val birthDate = try {
            LocalDate.parse("$strYear$strMonth$strDay", yyyyMMdd)
        } catch (e: Exception) {
            logger.error { "身份证号码格式错误，出生日期不正确" }
            return null
        }

        // 判断最后一位的值
        var aiWi = 0
        for (i in 0..16) {
            aiWi += ai[i].toString().toInt() * W[i].toInt()
        }
        val modValue = aiWi % 11
        if (CheckCodes[modValue].uppercase(Locale.getDefault()) != checkCode) {
            logger.error { "身份证号码格式错误，校验位不正确" }
            return null
        }

        // 计算出生日期是否有效, 在当前时间之后直接报错
        val now = LocalDate.now()
        if (birthDate.isBefore(now).not()) {
            logger.error { "身份证号码格式错误，出生日期不正确" }
            return null
        }

        // 计算周岁年龄
        var age = now.year - birthDate.year
        if (now.minusYears(age.toLong()).isBefore(birthDate)) {
            // 生日还没过则周岁减一
            age--
        }

        return IdCard(
            areaCode = areaCode,
            province = ProvinceCodes[provinceCode]!!,
            birthday = birthDate,
            age = age,
        )
    }

    data class IdCard(
        val areaCode: String,
        val province: String,
        val birthday: LocalDate,
        val age: Int,
    )

}