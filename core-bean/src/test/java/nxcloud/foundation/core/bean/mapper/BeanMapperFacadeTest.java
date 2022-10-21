package nxcloud.foundation.core.bean.mapper;

import lombok.Data;
import lombok.NoArgsConstructor;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import nxcloud.foundation.core.bean.mapper.impl.orika.OrikaBeanMapperFacadeImpl;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

public class BeanMapperFacadeTest {

    BeanMapperFacade beanMapper = new OrikaBeanMapperFacadeImpl(new DefaultMapperFactory.Builder().build());

//    BeanMapperFacade beanMapper = new DozerBeanMapperFacadeImpl(DozerBeanMapperBuilder.buildDefault());

    @Test
    public void test() {
        Source src = new Source("Baeldung", 10);
        src.setChildren(new ArrayList<>());
        Dest dest = beanMapper.map(src, Dest.class);

        assertEquals(dest.getAge(), src.getAge());
        assertEquals(dest.getName(), src.getName());
        assertNotSame(dest.getChildren(), src.getChildren());
    }


}

@Data
class Source {
    private String name;
    private int age;

    private List<String> children;

    public Source(String name, int age) {
        this.name = name;
        this.age = age;
    }
}

@NoArgsConstructor
@Data
class Dest {
    private String name;
    private int age;

    private List<String> children;

    public Dest(String name, int age) {
        this.name = name;
        this.age = age;
    }
}