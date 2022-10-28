package nxcloud.foundation.core.spring.boot.autoconfigure.support;

import nxcloud.foundation.core.spring.support.SpringContextHelper;
import nxcloud.foundation.core.spring.support.SpringContextHelperAware;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NXSpringSupportAutoConfigurationJavaTest {

    public static void main(String[] args) {
        SpringApplication.run(NXSpringSupportAutoConfigurationJavaTest.class, args);
        SpringContextHelperAware aware = SpringContextHelper.getBean("springContextHelperAware");
        System.out.println(aware);
    }

}
