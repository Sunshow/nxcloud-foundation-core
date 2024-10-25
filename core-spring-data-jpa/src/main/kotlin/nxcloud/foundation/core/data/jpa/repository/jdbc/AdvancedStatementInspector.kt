package nxcloud.foundation.core.data.jpa.repository.jdbc

import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.statement.select.PlainSelect
import org.hibernate.resource.jdbc.spi.StatementInspector

class AdvancedStatementInspector : StatementInspector {

    override fun inspect(sql: String): String {
        val parsed = CCJSqlParserUtil.parse(sql)
        // 动态添加 deleted = 0查询条件
        if (parsed is PlainSelect) {
            val deleted = "${parsed.fromItem.alias}.deleted = 0"
            parsed.where = if (parsed.where == null) {
                CCJSqlParserUtil.parseCondExpression(deleted)
            } else {
                CCJSqlParserUtil.parseCondExpression("$deleted AND ${parsed.where}")
            }
            return parsed.toString()
        }

        return sql
    }

}