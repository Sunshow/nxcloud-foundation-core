package springframework.child.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ConfigurableApplicationContext


@SpringBootApplication
class TestMultiContextApp {

    companion object {
        lateinit var args: Array<String>
        lateinit var applicationContext: ConfigurableApplicationContext

        fun restart() {
            val thread = Thread {
                applicationContext.close()
                applicationContext = runApplication<TestMultiContextApp>(*args)
            }

            thread.isDaemon = false
            thread.start()
        }
    }

}

fun main(args: Array<String>) {
    TestMultiContextApp.args = args
    TestMultiContextApp.applicationContext = runApplication<TestMultiContextApp>(*args)
}
