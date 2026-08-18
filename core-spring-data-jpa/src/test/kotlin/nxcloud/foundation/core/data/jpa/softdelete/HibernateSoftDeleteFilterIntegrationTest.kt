package nxcloud.foundation.core.data.jpa.softdelete

import jakarta.persistence.Entity
import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PersistenceContext
import jakarta.persistence.Table
import nxcloud.foundation.core.data.jpa.entity.DeletedField
import nxcloud.foundation.core.data.support.annotation.EnableSoftDelete
import nxcloud.foundation.core.data.support.context.DataQueryContext
import nxcloud.foundation.core.data.support.context.DataQueryContextHolder
import nxcloud.foundation.core.data.support.enumeration.DataQueryMode
import nxcloud.foundation.core.spring.boot.autoconfigure.support.NXSpringDataJpaAutoConfiguration
import nxcloud.foundation.core.spring.boot.autoconfigure.support.NXSpringSupportAutoConfiguration
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DataJpaTest(properties = ["spring.jpa.open-in-view=false"])
@AutoConfigureTestDatabase
@ContextConfiguration(classes = [SoftDeleteFilterTestApplication::class])
@ImportAutoConfiguration(
    classes = [
        NXSpringSupportAutoConfiguration::class,
        NXSpringDataJpaAutoConfiguration::class,
    ]
)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class HibernateSoftDeleteFilterIntegrationTest {

    @Autowired
    private lateinit var repository: FilterTestEntityRepository

    @Autowired
    private lateinit var transactionManager: PlatformTransactionManager

    @Autowired
    private lateinit var entityManagerFactory: EntityManagerFactory

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @AfterEach
    fun cleanUp() {
        DataQueryContextHolder.reset()
        jdbcTemplate.update("DELETE FROM test_soft_delete_filter")
        jdbcTemplate.update("DELETE FROM test_unfiltered_entity")
    }

    @Test
    fun `not-deleted mode filters repository queries and find by id across sessions`() {
        val activeId = insert("active", 0)
        val deletedId = insert("deleted", 1_000)

        DataQueryContextHolder.reset(enabled = true)

        inTransaction {
            assertEquals(listOf(activeId), repository.findAllByOrderById().map { it.id })
            assertNotNull(entityManager.find(FilterTestEntity::class.java, activeId))
            assertNull(entityManager.find(FilterTestEntity::class.java, deletedId))
            assertTrue(repository.findById(deletedId).isEmpty)
        }
    }

    @Test
    fun `deleted mode applies the configured deletion time range`() {
        val activeId = insert("active", 0)
        val oldDeletedId = insert("old-deleted", 1_000)
        val matchingDeletedId = insert("matching-deleted", 2_000)

        DataQueryContextHolder.set(
            DataQueryContext(
                enable = true,
                queryMode = DataQueryMode.Deleted,
                deletedAfter = 1_500,
                deletedBefore = 2_500,
            )
        )

        inTransaction {
            assertEquals(listOf(matchingDeletedId), repository.findAllByOrderById().map { it.id })
            assertNull(entityManager.find(FilterTestEntity::class.java, activeId))
            assertNull(entityManager.find(FilterTestEntity::class.java, oldDeletedId))
            assertNotNull(entityManager.find(FilterTestEntity::class.java, matchingDeletedId))
        }
    }

    @Test
    fun `only explicit none and disabled contexts leave soft-delete rows unfiltered`() {
        val activeId = insert("active", 0)
        val deletedId = insert("deleted", 1_000)

        DataQueryContextHolder.set(
            DataQueryContext(
                enable = true,
                queryMode = DataQueryMode.None,
            )
        )
        inTransaction {
            assertEquals(listOf(activeId, deletedId), repository.findAllByOrderById().map { it.id })
        }

        DataQueryContextHolder.reset()
        inTransaction {
            assertEquals(listOf(activeId), repository.findAllByOrderById().map { it.id })
        }

        DataQueryContextHolder.set(DataQueryContext(enable = false))
        inTransaction {
            assertEquals(listOf(activeId, deletedId), repository.findAllByOrderById().map { it.id })
        }
    }

    @Test
    fun `manual entity managers are initialized without open-session-in-view`() {
        val activeId = insert("active", 0)
        val deletedId = insert("deleted", 1_000)

        DataQueryContextHolder.set(
            DataQueryContext(
                enable = true,
                queryMode = DataQueryMode.Deleted,
                deletedAfter = 0,
            )
        )

        val manualEntityManager = entityManagerFactory.createEntityManager()
        try {
            assertNull(manualEntityManager.find(FilterTestEntity::class.java, activeId))
            assertNotNull(manualEntityManager.find(FilterTestEntity::class.java, deletedId))
        } finally {
            manualEntityManager.close()
        }
    }

    @Test
    fun `entities without the marker annotation are not filtered`() {
        val entityId = inTransaction {
            val entity = UnfilteredTestEntity(name = "deleted", deleted = 1_000)
            entityManager.persist(entity)
            entityManager.flush()
            entity.id
        }

        DataQueryContextHolder.reset(enabled = true)

        inTransaction {
            assertNotNull(entityManager.find(UnfilteredTestEntity::class.java, entityId))
        }
    }

    @Test
    fun `delete by id treats an already deleted entity as absent`() {
        val deletedId = insert("deleted", 1_000)

        DataQueryContextHolder.reset(enabled = true)

        inTransaction {
            repository.deleteById(deletedId)
            repository.flush()
            assertTrue(repository.findById(deletedId).isEmpty)
        }

        assertEquals(
            1_000,
            jdbcTemplate.queryForObject(
                "SELECT deleted FROM test_soft_delete_filter WHERE id = ?",
                Long::class.java,
                deletedId,
            )
        )
    }

    private fun insert(name: String, deleted: Long): Long = inTransaction {
        repository.saveAndFlush(FilterTestEntity(name = name, deleted = deleted)).id
    }

    private fun <T : Any> inTransaction(block: () -> T): T {
        return TransactionTemplate(transactionManager).execute { block() }
            ?: error("Transaction returned null")
    }
}

@EnableSoftDelete
@MappedSuperclass
abstract class MarkerSoftDeleteEntity : DeletedField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    override var deleted: Long = 0
}

@Entity
@Table(name = "test_soft_delete_filter")
class FilterTestEntity(
    var name: String,
    override var deleted: Long = 0,
) : MarkerSoftDeleteEntity()

@Entity
@Table(name = "test_unfiltered_entity")
class UnfilteredTestEntity(
    var name: String,
    var deleted: Long = 0,
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
}

@Repository
interface FilterTestEntityRepository : JpaRepository<FilterTestEntity, Long> {

    fun findAllByOrderById(): List<FilterTestEntity>
}

@SpringBootConfiguration
@EnableAutoConfiguration
@EntityScan(basePackageClasses = [FilterTestEntity::class])
@EnableJpaRepositories(basePackageClasses = [FilterTestEntityRepository::class])
class SoftDeleteFilterTestApplication
