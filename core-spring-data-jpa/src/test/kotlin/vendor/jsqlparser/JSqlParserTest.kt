package vendor.jsqlparser

import net.sf.jsqlparser.parser.CCJSqlParserUtil
import kotlin.test.Test

class JSqlParserTest {

    @Test
    fun testParse() {
        val sql =
            "select te1_0.id,te1_0.additional_info,te1_0.address,te1_0.address2,te1_0.city,te1_0.country,te1_0.created_time,te1_0.email,te1_0.phone,te1_0.region,te1_0.state,te1_0.tenant_profile_id,te1_0.title,te1_0.version,te1_0.zip from tenant te1_0 where (? is null or (te1_0.title::text ILIKE ('%'||?||'%')::text)=true) order by te1_0.id fetch first ? rows only"
        val parsed = CCJSqlParserUtil
            .parse(sql) {
                // 关闭复杂语法校验（适用于简单或已知正确的 SQL）
                it.withAllowComplexParsing(false)
            }
        println(parsed)
    }

}