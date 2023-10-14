package nxcloud.foundation.core.spring.support.property.wechat

/**
 * 微信支付相关配置
 */
data class WechatPayProperty(
    val appid: String,
    val merchantId: String,
    val merchantSerialNumber: String,
    val privateKey: String,
    val apiV3Key: String,
    val notifyUrl: String = "",
)
