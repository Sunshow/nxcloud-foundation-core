package nxcloud.foundation.core.universal.task.registry

import io.github.oshai.kotlinlogging.KotlinLogging
import nxcloud.foundation.core.universal.task.spi.UniversalTaskIndicator
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object UniversalTaskIndicatorRegistry {

    private val logger = KotlinLogging.logger {}

    private val registeredIndicators = ConcurrentHashMap<String, UniversalTaskIndicator>()

    init {
        // 通过 SPI 加载扩展点
        loadFromSPI()
    }

    fun register(indicator: UniversalTaskIndicator): Boolean {
        val name = indicator.indicatorId()

        return if (registeredIndicators.containsKey(name)) {
            false
        } else {
            registeredIndicators[name] = indicator
            true
        }
    }

    fun unregister(indicatorName: String): UniversalTaskIndicator? {
        return registeredIndicators.remove(indicatorName)
    }

    fun getIndicator(indicatorId: String): UniversalTaskIndicator? {
        return registeredIndicators[indicatorId]
    }

    fun getIndicatorList(): List<UniversalTaskIndicator> {
        return registeredIndicators.values.toList()
    }

    private fun loadFromSPI() {
        try {
            val serviceLoader = ServiceLoader.load(UniversalTaskIndicator::class.java)
            for (indicator in serviceLoader) {
                runCatching {
                    val indicatorName = indicator.indicatorId()
                    if (!registeredIndicators.containsKey(indicatorName)) {
                        registeredIndicators[indicatorName] = indicator
                        logger.info {
                            "Loaded UniversalTaskIndicator via SPI: $indicatorName, ${indicator.indicatorName()}"
                        }
                    }
                }.onFailure {
                    logger.error(it) {
                        "Failed to load UniversalTaskIndicator via SPI: ${indicator.javaClass.name}, ${indicator.indicatorName()}, error: ${it.message}"
                    }
                }
            }
        } catch (e: Exception) {
            logger.error(e) {
                "Failed to load UniversalTaskIndicators via SPI: ${e.message}"
            }
        }
    }

}