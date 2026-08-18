package nxcloud.foundation.core.data.jpa.softdelete

import jakarta.persistence.EntityManager
import nxcloud.foundation.core.data.jpa.constant.JpaConstants
import nxcloud.foundation.core.data.support.context.DataQueryContextHolder
import nxcloud.foundation.core.data.support.enumeration.DataQueryMode
import org.hibernate.Session

/**
 * Configures filters from the query context captured when an EntityManager is created.
 * A context change applies to the next EntityManager, not an already-open one.
 */
class HibernateSoftDeleteEntityManagerInitializer {

    fun initialize(entityManager: EntityManager) {
        val context = DataQueryContextHolder.current() ?: return
        val session = entityManager.unwrap(Session::class.java)

        when {
            !context.enable || context.queryMode == DataQueryMode.None -> {
                session.disableFilter(JpaConstants.FILTER_NOT_DELETED)
            }

            context.queryMode == DataQueryMode.NotDeleted -> {
                session.enableFilter(JpaConstants.FILTER_NOT_DELETED)
            }

            context.queryMode == DataQueryMode.Deleted -> {
                session.disableFilter(JpaConstants.FILTER_NOT_DELETED)
                session
                    .enableFilter(JpaConstants.FILTER_DELETED_AFTER)
                    .setParameter(
                        JpaConstants.FILTER_PARAMETER_DELETED_AFTER,
                        context.deletedAfter,
                    )

                if (context.deletedBefore > 0) {
                    session
                        .enableFilter(JpaConstants.FILTER_DELETED_BEFORE)
                        .setParameter(
                            JpaConstants.FILTER_PARAMETER_DELETED_BEFORE,
                            context.deletedBefore,
                        )
                }
            }
        }
    }
}
