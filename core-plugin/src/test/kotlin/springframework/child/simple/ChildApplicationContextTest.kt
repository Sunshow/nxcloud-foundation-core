package springframework.child.simple

import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.GenericApplicationContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class ChildApplicationContextTest {

    private val parentApplicationContext by lazy {
        val applicationContext = AnnotationConfigApplicationContext()
        applicationContext.register(ParentConfiguration::class.java)
        applicationContext.refresh()

        applicationContext
    }

    @Test
    fun testCreateParent() {
        println(parentApplicationContext)
        println(parentApplicationContext.getBean(ParentBean::class.java))
    }

    private fun createChild1ApplicationContext(customizer: ((AnnotationConfigApplicationContext) -> Unit)? = null): GenericApplicationContext {
        val childApplicationContext = AnnotationConfigApplicationContext()
        childApplicationContext.parent = parentApplicationContext
        childApplicationContext.register(Child1Configuration::class.java)

        customizer?.invoke(childApplicationContext)

        childApplicationContext.refresh()

        return childApplicationContext
    }

    private fun createChild2ApplicationContext(customizer: ((AnnotationConfigApplicationContext) -> Unit)? = null): GenericApplicationContext {
        val childApplicationContext = AnnotationConfigApplicationContext()
        childApplicationContext.parent = parentApplicationContext
        childApplicationContext.register(Child2Configuration::class.java)

        customizer?.invoke(childApplicationContext)

        childApplicationContext.refresh()

        return childApplicationContext
    }

    @Test
    fun testChildGetParentBean() {
        val childApplicationContext = createChild1ApplicationContext()

        println(childApplicationContext)
        println(childApplicationContext.getBean(ParentBean::class.java))

        println(childApplicationContext.getBean(ChildBean::class.java))
    }

    @Test
    fun testChildRecreate() {
        var childApplicationContext = createChild1ApplicationContext()

        println(childApplicationContext)
        println(childApplicationContext.getBean(ParentBean::class.java))

        println(childApplicationContext.getBean(ChildBean::class.java))

        childApplicationContext.close()

        childApplicationContext = createChild1ApplicationContext()

        println(childApplicationContext)
        println(childApplicationContext.getBean(ParentBean::class.java))

        println(childApplicationContext.getBean(ChildBean::class.java))
    }

    @Test
    fun testMultiChildIsolate() {
        val child1ApplicationContext = createChild1ApplicationContext()
        val child2ApplicationContext = createChild2ApplicationContext()

        assertNotNull(child1ApplicationContext.getBean(ParentBean::class.java))
        assertNotNull(child2ApplicationContext.getBean(ParentBean::class.java))
        assertEquals(
            child1ApplicationContext.getBean(ParentBean::class.java),
            child2ApplicationContext.getBean(ParentBean::class.java)
        )

        assertNotNull(child1ApplicationContext.getBean("child1Bean"))
        assertNotNull(child2ApplicationContext.getBean("child2Bean"))

        assertFailsWith(NoSuchBeanDefinitionException::class) {
            parentApplicationContext.getBean("child1Bean")
        }

        assertFailsWith(NoSuchBeanDefinitionException::class) {
            parentApplicationContext.getBean("child2Bean")
        }

        assertFailsWith(NoSuchBeanDefinitionException::class) {
            child1ApplicationContext.getBean("child2Bean")
        }
        assertFailsWith(NoSuchBeanDefinitionException::class) {
            child2ApplicationContext.getBean("child1Bean")
        }

    }

}

@Configuration
class ParentConfiguration {

    @Bean
    protected fun parentBean(): ParentBean {
        return ParentBean()
    }

}

@Configuration
class Child1Configuration {

    @Bean
    protected fun child1Bean(): ChildBean {
        return ChildBean()
    }

}

@Configuration
class Child2Configuration {

    @Bean
    protected fun child2Bean(): ChildBean {
        return ChildBean()
    }

}


class ParentBean

class ChildBean