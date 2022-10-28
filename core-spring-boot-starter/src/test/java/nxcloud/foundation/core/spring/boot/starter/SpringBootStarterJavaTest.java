package nxcloud.foundation.core.spring.boot.starter;

import nxcloud.foundation.core.bean.mapper.BeanMapperFacade;
import nxcloud.foundation.core.spring.support.SpringContextHelper;
import nxcloud.foundation.core.spring.support.SpringContextHelperAware;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBootStarterJavaTest {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootStarterJavaTest.class, args);
        SpringContextHelperAware aware = SpringContextHelper.getBean("springContextHelperAware");
        System.out.println(aware);
        BeanMapperFacade beanMapper = SpringContextHelper.getBean(BeanMapperFacade.class);
        System.out.println(beanMapper);
    }

}
