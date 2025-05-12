package nxcloud.foundation.core.data.jpa.repository.jdbc

import io.github.oshai.kotlinlogging.KotlinLogging
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.parser.ParseException
import net.sf.jsqlparser.schema.Table
import net.sf.jsqlparser.statement.select.PlainSelect
import nxcloud.foundation.core.data.jpa.repository.support.JpaEntitySupporter
import nxcloud.foundation.core.data.support.context.DataQueryContext
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
        val context = DataQueryContextHolder.currentOrElse()

        if (context.enable) {
            logger.debug { "已启用 DataQueryContext, 开始处理, context=$context" }

            try {
                val parsed = CCJSqlParserUtil.parse(sql) {
                    // 关闭复杂语法校验（适用于简单或已知正确的 SQL）
                    it.withAllowComplexParsing(false)
                }
                // 动态添加 deleted = 0查询条件
                if (parsed is PlainSelect) {
                    return if (composeWithDataQueryContext(parsed, context)) {
                        parsed.toString()
                    } else {
                        sql
                    }
                }
            } catch (e: Exception) {
                if (e is ParseException) {
                    logger.debug {
                        "解析SQL语句失败, 不做处理, 原始SQL: $sql, 错误信息: ${e.message}"
                    }
                } else {
                    logger.error(e) {
                        "解析SQL语句失败, 不做处理, 原始SQL: $sql"
                    }
                }
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
    private fun composeWithDataQueryContext(plainSelect: PlainSelect, context: DataQueryContext): Boolean {
        val fromTable = plainSelect.fromItem as? Table ?: return false

        val tableName = fromTable.name

        val metadata = jpaEntitySupporter.getMetadataByPhysicalTableName(tableName) ?: return false

        if (!metadata.enableSoftDelete) {
            return false
        }

        logger.debug { "启用全局软删除处理" }

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