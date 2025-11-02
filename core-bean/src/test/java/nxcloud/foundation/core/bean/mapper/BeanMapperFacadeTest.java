package nxcloud.foundation.core.bean.mapper;

import nxcloud.foundation.core.bean.mapper.impl.modelmapper.ModelMapperBeanMapperFacadeImpl;
import nxcloud.foundation.core.lang.enumeration.YesNoStatus;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

public class BeanMapperFacadeTest {

//    BeanMapperFacade beanMapper = new OrikaBeanMapperFacadeImpl(new DefaultMapperFactory.Builder().build());

//    BeanMapperFacade beanMapper = new DozerBeanMapperFacadeImpl(DozerBeanMapperBuilder.buildDefault());

    BeanMapperFacade beanMapper = new ModelMapperBeanMapperFacadeImpl(new ModelMapper());

    @Test
    public void test() {
        Source src = new Source("Baeldung", 10);
        src.setStatus(YesNoStatus.Yes.INSTANCE);
        src.setChildren(new ArrayList<>());
        Dest dest = beanMapper.map(src, Dest.class);

        assertEquals(dest.getAge(), src.getAge());
        assertEquals(dest.getName(), src.getName());
        assertEquals(dest.getStatus(), src.getStatus());
        assertNotSame(dest.getChildren(), src.getChildren());
    }


}

class Source {
    private String name;
    private int age;

    private YesNoStatus status;

    private List<String> children;

    public Source(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public YesNoStatus getStatus() {
        return status;
    }

    public void setStatus(YesNoStatus status) {
        this.status = status;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }
}

class Dest {
    private String name;
    private int age;

    private YesNoStatus status;

    private List<String> children;

    public Dest() {
    }

    public Dest(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public YesNoStatus getStatus() {
        return status;
    }

    public void setStatus(YesNoStatus status) {
        this.status = status;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }
}