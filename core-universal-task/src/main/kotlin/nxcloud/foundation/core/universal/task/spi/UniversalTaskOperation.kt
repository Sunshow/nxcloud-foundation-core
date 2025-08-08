package nxcloud.foundation.core.universal.task.spi

enum class UniversalTaskOperation(
    val code: String,
    val description: String
) {
    START("start", "启动"),
    PAUSE("pause", "暂停"),
    STOP("stop", "停止"),
    RESTART("restart", "重启"),
    RESET("reset", "重置")
}