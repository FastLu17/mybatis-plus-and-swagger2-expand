package com.luxf.mybatis.plus.config.refresh;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import org.apache.ibatis.mapping.MappedStatement;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * 在{@link MybatisPlusAutoConfiguration#afterPropertiesSet()}方法中,
 * 会执行{@link com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer#customize(MybatisPlusProperties)}方法。
 * <p>
 * 在afterPropertiesSet()方法中, 替换掉{@link MybatisConfiguration}正合适、
 * <p>
 * 在{@link MybatisPlusAutoConfiguration#sqlSessionFactory(DataSource)}方法中,
 * 执行applyConfiguration(factory)方法时, 会使用{@link MybatisPlusProperties#getConfiguration()}
 *
 * @author 小66
 * @Description
 * @create 2021-03-12 21:07
 **/
@Component
public class MybatisPlusPropertiesCustomizer implements com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer {
    @Override
    public void customize(MybatisPlusProperties properties) {
        MybatisConfiguration configuration = properties.getConfiguration();
        CustomConfiguration customConfiguration = new CustomConfiguration();
        // 复制MybatisConfiguration的属性, 保证功能的完整性.
        BeanUtils.copyProperties(configuration, customConfiguration);
        // 使用自定义的CustomConfiguration, 即可开启刷新功能.
        properties.setConfiguration(customConfiguration);
    }

    public static class CustomConfiguration extends MybatisConfiguration {
        /**
         * 使用{@link org.apache.ibatis.session.Configuration#addMappedStatement(MappedStatement)}即可正常使用刷新xml的功能、
         */
        @Override
        public void addMappedStatement(MappedStatement ms) {
            mappedStatements.put(ms.getId(), ms);
        }
    }
}
