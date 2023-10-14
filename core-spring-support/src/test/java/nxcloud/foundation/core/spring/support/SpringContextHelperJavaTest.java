package nxcloud.foundation.core.spring.support;

import nxcloud.foundation.core.spring.support.context.SpringContextHelper;
import nxcloud.foundation.core.spring.support.context.SpringContextHelperAware;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class SpringContextHelperJavaTest {

    @Test
    void test() {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(SpringContextHelperAware.class);
        applicationContext.refresh();
        SpringContextHelperAware aware = SpringContextHelper.getBean("springContextHelperAware");
        Assertions.assertNotNull(aware);
    }

}
