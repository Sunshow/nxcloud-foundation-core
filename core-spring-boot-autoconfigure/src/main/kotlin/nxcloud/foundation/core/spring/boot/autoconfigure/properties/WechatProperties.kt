package nxcloud.foundation.core.spring.boot.autoconfigure.properties

import nxcloud.foundation.core.spring.support.property.wechat.WechatMiniProperty
import nxcloud.foundation.core.spring.support.property.wechat.WechatPayProperty
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = WechatProperties.PREFIX)
data class WechatProperties(
    val mini: Map<String, WechatMiniProperty> = emptyMap(),
    val pay: Map<String, WechatPayProperty> = emptyMap(),
) {
    companion object {
        const val PREFIX = "nxcloud.wechat"
    }

}
