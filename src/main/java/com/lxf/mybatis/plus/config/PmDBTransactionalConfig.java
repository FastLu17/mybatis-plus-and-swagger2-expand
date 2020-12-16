package com.lxf.mybatis.plus.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Collections;

/**
 * 事务配置,默认 : 开启
 *
 * @author luxf
 * @date 2020-12-07 17:35
 */
@Configuration
@Slf4j
public class PmDBTransactionalConfig {


    private static final String CUSTOMIZE_COMMON_TRANSACTION_INTERCEPTOR_NAME = "customizeCommonTransactionInterceptor";
    /**
     * 默认只对 "*Service" , "*ServiceImpl" Bean 进行事务处理,"*"表示模糊匹配, 比如 : userService,orderServiceImpl
     */
    private static final String[] DEFAULT_TRANSACTION_BEAN_NAMES = {"*Service", "*ServiceImpl"};

    /**
     * 动态代理Creator的通用拦截器的 是否创建代理对象的过滤规则、
     */
    private static final String BEAN_CLASS_PREFIX = "com.luxf";

    /**
     * 可传播事务配置
     */
    private static final String[] DEFAULT_REQUIRED_METHOD_RULE_TRANSACTION_ATTRIBUTES = {"do*", "add*", "save*",
            "insert*", "delete*", "update*", "edit*", "batch*", "create*", "remove*"};
    /**
     * 默认的只读事务
     */
    private static final String[] DEFAULT_READ_ONLY_METHOD_RULE_TRANSACTION_ATTRIBUTES = {"get*", "count*", "find*",
            "query*", "select*", "list*"};

    /**
     * 如果添加的持久化依赖比较多, 手动指定事务管理器、
     * <p>
     * 如果你添加的是 spring-boot-starter-jdbc 依赖，框架会默认注入 DataSourceTransactionManager 实例。
     * 如果你添加的是 spring-boot-starter-data-jpa 依赖，框架会默认注入 JpaTransactionManager 实例。
     *
     * @param dataSource dataSource
     */
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * 配置事务拦截器、
     * {@link Transactional}主要实现就是 TransactionInterceptor
     *
     * @param transactionManager 事务管理器
     */
    @Bean(CUSTOMIZE_COMMON_TRANSACTION_INTERCEPTOR_NAME)
    public TransactionInterceptor customizeTransactionInterceptor(PlatformTransactionManager transactionManager) {
        NameMatchTransactionAttributeSource transactionAttributeSource = new NameMatchTransactionAttributeSource();
        RuleBasedTransactionAttribute readOnly = this.readOnlyTransactionRule();
        RuleBasedTransactionAttribute required = this.requiredTransactionRule();

        // 默认的只读事务配置(查询)
        for (String methodName : DEFAULT_READ_ONLY_METHOD_RULE_TRANSACTION_ATTRIBUTES) {
            transactionAttributeSource.addTransactionalMethod(methodName, readOnly);
        }

        // 默认的传播事务配置(增删改)
        for (String methodName : DEFAULT_REQUIRED_METHOD_RULE_TRANSACTION_ATTRIBUTES) {
            transactionAttributeSource.addTransactionalMethod(methodName, required);
        }

        return new TransactionInterceptor(new IPlatformTransactionManager(transactionManager), transactionAttributeSource);
    }

    /**
     * 配置动态代理Creator、
     * <p>
     * 主要是 BeanNameAutoProxyCreator extends {@link AbstractAutoProxyCreator}
     * AbstractAutoProxyCreator implements {@link SmartInstantiationAwareBeanPostProcessor}
     */
    @Bean
    public BeanNameAutoProxyCreator customizeTransactionBeanNameAutoProxyCreator() {

        BeanNameAutoProxyCreator beanNameAutoProxyCreator = new IBeanNameAutoProxyCreator();

        // 设置自定义的通用事务拦截器、(如果没有设置该拦截器, 无法进行拦截)
        /**
         * Default is no common interceptors(默认没有通用拦截器、)
         * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#interceptorNames
         */
        beanNameAutoProxyCreator.setInterceptorNames(CUSTOMIZE_COMMON_TRANSACTION_INTERCEPTOR_NAME);

        /**
         * 设置需要动态代理的 beanName、
         * @see BeanNameAutoProxyCreator#getAdvicesAndAdvisorsForBean(Class, String, TargetSource) 会根据beanNames匹配、符合规则的Bean就生成代理类
         */
        beanNameAutoProxyCreator.setBeanNames(DEFAULT_TRANSACTION_BEAN_NAMES);

        beanNameAutoProxyCreator.setProxyTargetClass(true);
        return beanNameAutoProxyCreator;
    }

    /**
     * 支持当前事务、如果不存在创建一个新的
     * {@link org.springframework.transaction.annotation.Propagation#REQUIRED}
     */
    private RuleBasedTransactionAttribute requiredTransactionRule() {
        RuleBasedTransactionAttribute required = new RuleBasedTransactionAttribute();
        required.setRollbackRules(Collections.singletonList(new RollbackRuleAttribute(Exception.class)));
        required.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        required.setTimeout(TransactionDefinition.TIMEOUT_DEFAULT);
        return required;
    }

    /**
     * 只读事务(不会执行{@link DataSourceTransactionManager#doBegin(Object, TransactionDefinition)}方法)
     * {@link org.springframework.transaction.annotation.Propagation#NOT_SUPPORTED}
     */
    private RuleBasedTransactionAttribute readOnlyTransactionRule() {
        RuleBasedTransactionAttribute readOnly = new RuleBasedTransactionAttribute();
        readOnly.setReadOnly(true);
        readOnly.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
        return readOnly;
    }

    /**
     * 重写一下createProxy()方法, 不符合规则就不代理、
     */
    private class IBeanNameAutoProxyCreator extends BeanNameAutoProxyCreator {

        @Override
        protected Object createProxy(Class beanClass, String beanName, Object[] specificInterceptors,
                                     TargetSource targetSource) {
            try {
                Object target = targetSource.getTarget();
                if (beanClass.getName().startsWith(BEAN_CLASS_PREFIX)) {
                    return super.createProxy(beanClass, beanName, specificInterceptors, targetSource);
                } else {
                    log.info("BeanNameAutoProxyCreator Ignore Bean: " + beanName);
                    return target;
                }
            } catch (Throwable ex) {
                return super.createProxy(beanClass, beanName, specificInterceptors, targetSource);
            }
        }
    }

    /**
     * 代理一下{@link PlatformTransactionManager}, 可以简单的打一下日志、
     * 如果有需要, 可以通过继承{@link AbstractPlatformTransactionManager}自定义复杂的实现、
     */
    private class IPlatformTransactionManager implements PlatformTransactionManager {

        private PlatformTransactionManager delegate;

        IPlatformTransactionManager(PlatformTransactionManager delegate) {
            this.delegate = delegate;
        }

        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {

            if (log.isDebugEnabled()) {
                log.info("事物申请getTransaction->" + definition.getName());
            }
            return delegate.getTransaction(definition);
        }

        @Override
        public void commit(TransactionStatus status) throws TransactionException {
            delegate.commit(status);

            if (log.isDebugEnabled()) {
                log.info("事物提交");
            }
        }

        @Override
        public void rollback(TransactionStatus status) throws TransactionException {
            delegate.rollback(status);
            if (log.isDebugEnabled()) {
                log.info("事物回滚");
            }
        }
    }
}
