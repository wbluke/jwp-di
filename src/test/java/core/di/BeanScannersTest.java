package core.di;

import core.di.factory.example.IntegrationConfig;
import core.di.factory.example.JdbcQuestionRepository;
import core.di.factory.example.JdbcUserRepository;
import core.di.factory.example.MyJdbcTemplate;
import core.di.factory.example.MyQnaService;
import core.di.factory.example.QnaController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BeanScannersTest {

    @DisplayName("BeanScanner 와 ConfigurationBeanScanner 를 통해 기준 package 하위에 있는 모든 Bean들을 스캔한다.")
    @Test
    void scan() {
        /* given */
        BeanScanners beanScanners = createBeanScanners("core.di.factory.example");

        /* when */
        Set<Class<?>> preInstantiateBeans = beanScanners.scan();

        /* then */
        assertThat(preInstantiateBeans).hasSize(6);
        assertThat(preInstantiateBeans).containsExactlyInAnyOrder(JdbcQuestionRepository.class, MyJdbcTemplate.class,
                QnaController.class, JdbcUserRepository.class, MyQnaService.class, DataSource.class);
    }

    @DisplayName("Bean 스캔 시 중복되는 Bean 후보가 있다면 Exception")
    @Test
    void scan_exception() {
        /* given */
        BeanScanners beanScanners = createBeanScanners("core.di.factory.illegal.configuration");

        /* when */ /* then */
        assertThrows(IllegalStateException.class, beanScanners::scan);
    }

    @DisplayName("특정 Bean 후보의 파라미터 타입 가져오기")
    @Test
    void getParameterTypesForInstantiation() {
        /* given */
        BeanScanners beanScanners = createBeanScanners("core.di.factory.example");
        beanScanners.scan();

        /* when */
        Class<?>[] parameterTypes = beanScanners.getParameterTypesForInstantiation(MyJdbcTemplate.class);

        /* then */
        assertThat(parameterTypes).hasSize(1);
        assertThat(parameterTypes).containsExactly(DataSource.class);
    }

    @DisplayName("인스턴스 생성하기")
    @Test
    void instantiate() {
        /* given */
        BeanScanners beanScanners = createBeanScanners("core.di.factory.example");
        beanScanners.scan();

        DataSource dataSource = new IntegrationConfig().dataSource();

        /* when */
        Object instance = beanScanners.instantiate(MyJdbcTemplate.class, dataSource);

        /* then */
        assertThat(instance).isNotNull();
    }

    private BeanScanners createBeanScanners(Object... basePackage) {
        BeanScanner beanScanner = new BeanScanner(basePackage);
        ConfigurationBeanScanner configurationBeanScanner = new ConfigurationBeanScanner(basePackage);

        return new BeanScanners(beanScanner, configurationBeanScanner);
    }

}
