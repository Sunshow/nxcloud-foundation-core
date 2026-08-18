package nxcloud.foundation.core.data.jpa.repository.jdbc

import org.hibernate.resource.jdbc.spi.StatementInspector

/**
 * Kept for applications that still reference the former statement inspector by class name.
 * Soft-delete filtering is now provided by Hibernate filters.
 */
@Deprecated("Soft-delete filtering is provided by HibernateSoftDeleteMetadataContributor")
class AdvancedStatementInspector : StatementInspector {

    override fun inspect(sql: String): String = sql
}
