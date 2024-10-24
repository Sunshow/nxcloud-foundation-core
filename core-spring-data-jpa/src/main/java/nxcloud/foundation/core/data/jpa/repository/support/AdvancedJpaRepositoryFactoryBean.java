package nxcloud.foundation.core.data.jpa.repository.support;

import jakarta.persistence.EntityManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.io.Serializable;

/**
 * @author sunshow
 */
public class AdvancedJpaRepositoryFactoryBean<R extends JpaRepository<T, I>, T,
        I extends Serializable> extends JpaRepositoryFactoryBean<R, T, I> {

    // Spring Data 使用 Java 反射设置属性，因此需要提供 getter 和 setter 方法 Workaround
    // https://github.com/spring-projects/spring-data-commons/issues/2994
    // 为了避免兼容性问题 暂时使用 Java 实现

    public AdvancedJpaRepositoryFactoryBean(Class<? extends R> repositoryInterface) {
        super(repositoryInterface);
    }

    @NotNull
    protected RepositoryFactorySupport createRepositoryFactory(@NotNull EntityManager em) {
        return new AdvancedJpaRepositoryFactory(em);
    }

}
