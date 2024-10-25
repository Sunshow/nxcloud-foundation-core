package nxcloud.foundation.core.data.jpa.repository.jdbc

import io.github.oshai.kotlinlogging.KotlinLogging
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.schema.Table
import net.sf.jsqlparser.statement.select.PlainSelect
import nxcloud.foundation.core.data.jpa.repository.support.JpaEntitySupporter
import nxcloud.foundation.core.data.support.context.DataQueryContextHolder
import nxcloud.foundation.core.data.support.enumeration.DataQueryMode
import nxcloud.foundation.core.spring.support.context.SpringContextHelper
import org.hibernate.resource.jdbc.spi.StatementInspector

class AdvancedStatementInspector : StatementInspector {

    private val logger = KotlinLogging.logger {}

    private val jpaEntitySupporter: JpaEntitySupporter by lazy {
        SpringContextHelper.getBean(JpaEntitySupporter::class.java)
    }

    override fun inspect(sql: String): String {
        val parsed = CCJSqlParserUtil.parse(sql)
        // 动态添加 deleted = 0查询条件
        if (parsed is PlainSelect) {
            return if (composeWithDataQueryContext(parsed)) {
                parsed.toString()
            } else {
                sql
            }
        }

        return sql
    }

    /**
     * 组合当前 `DataQueryContext` 中包含的额外查询条件到给定的 `PlainSelect` 对象中。
     *
     * @param plainSelect 要修改的 `PlainSelect` 对象，添加额外的查询条件
     * @return 布尔值，表示是否进行了变更
     */
    private fun composeWithDataQueryContext(plainSelect: PlainSelect): Boolean {
        val fromTable = plainSelect.fromItem as? Table ?: return false

        val tableName = fromTable.name

        val metadata = jpaEntitySupporter.getMetadataByPhysicalTableName(tableName) ?: return false

        if (!metadata.enableSoftDelete) {
            return false
        }

        logger.debug { "启用全局软删除处理" }

        val context = DataQueryContextHolder.currentOrElse()

        val tableAlias = fromTable.alias?.name
            ?.takeIf {
                it.isNotBlank()
            }
            ?: tableName

        val deleted = when (context.queryMode) {
            DataQueryMode.NotDeleted -> "$tableAlias.deleted = 0"


            DataQueryMode.Deleted -> "$tableAlias.deleted > ${context.deletedAfter}${
                if (context.deletedBefore > 0) {
                    " AND $tableAlias.deleted < ${context.deletedBefore}"
                } else {
                    ""
                }
            }"

            else -> ""
        }

        if (deleted.isNotBlank()) {
            plainSelect.where = if (plainSelect.where == null) {
                CCJSqlParserUtil.parseCondExpression(deleted)
            } else {
                CCJSqlParserUtil.parseCondExpression("$deleted AND ${plainSelect.where}")
            }
            return true
        }

        return false
    }

}