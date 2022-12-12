package nxcloud.foundation.core.data.jpa.repository

import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.Repository
import java.io.Serializable

/**
 * author: sunshow.
 */
@NoRepositoryBean
interface JpaRepository<T, ID : Serializable> : Repository<T, ID>