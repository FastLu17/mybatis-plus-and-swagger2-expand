package com.luxf.mybatis.plus.config.refresh;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.NestedIOException;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 修改mapper.xml后, 刷新mybatis的StrictMap相关内容、不用重启项目。
 * <p>
 * <p>
 * TODO: 在Mybatis-plus项目中,由于{@link MybatisConfiguration}重写了{@link Configuration#addMappedStatement(MappedStatement)}方法, 会引起刷新的功能失效、
 * <p>
 * 方法一: 需要自定义Configuration, 重写{@link MybatisConfiguration#addMappedStatement(MappedStatement)}, 以此保证刷新功能的正常使用、
 * 方法二：安装JRebel mybatisPlus extension 插件. (不再需要启用此刷新功能的代码)
 *
 * TODO: mybatis-plus源码中, 似乎存在MybatisMapperRefresh类(3.0.6版本上移除了该功能,不过最新快照版已加回来并打上废弃标识,3.1.0版本上已完全移除), 与此类的功能应该是类似的。
 *
 * @author 小66
 * @date 2020-08-13 11:29
 * @see MybatisPlusPropertiesCustomizer 覆盖{@link MybatisConfiguration}
 **/
@Component
@EnableConfigurationProperties({MybatisRefreshProperties.class, MybatisPlusProperties.class})
@SuppressWarnings("all")
public class MapperFileRefresh implements Runnable {

    /**
     * 是否启用Mapper.xml刷新功能
     */
    private final boolean enabled;
    /**
     * 延迟刷新
     */
    private final int delaySeconds;
    /**
     * 间隔时间
     */
    private final int intervalSeconds;

    /**
     * mapper文件的后缀名、用于匹配是否是的mapper文件、
     */
    private final String mapperSuffix;

    /**
     * 刷新启用后，是否启动了刷新线程
     */
    private static boolean refreshable;

    public MapperFileRefresh(SqlSession sqlSession, MybatisPlusProperties properties) {
        MybatisRefreshProperties refreshProperties = new MybatisRefreshProperties();
        refreshProperties.setEnabled(true);
        this.configuration = sqlSession.getConfiguration();
        this.enabled = refreshProperties.getEnabled();
        this.delaySeconds = refreshProperties.getDelaySeconds();
        this.intervalSeconds = refreshProperties.getIntervalSeconds();
        this.mapperSuffix = refreshProperties.getMapperSuffix();
        // 可以自己定义资源路径、存放mapper文件的路径、(直接使用mybatis解析的最好)、
        // Resource[] resources = new PathMatchingResourcePatternResolver().getResources("classpath*:com/luxf/**/mapper/");
        this.mapperLocations = properties.resolveMapperLocations();
        refreshable = this.enabled;
    }

    @PostConstruct
    public void postConstruct() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(this);
        executor.shutdown();
    }

    private static final Logger log = LoggerFactory.getLogger(MapperFileRefresh.class);

    /**
     * Mapper实际资源目录
     */
    private Set<String> location;
    /**
     * Mapper资源路径
     */
    private Resource[] mapperLocations;
    /**
     * MyBatis配置对象
     */
    private Configuration configuration;
    /**
     * 线程上次刷新时间
     */
    private Long lastRefreshTime;

    private static boolean isRefreshable() {
        return refreshable;
    }


    @Override
    public void run() {
        lastRefreshTime = System.currentTimeMillis();
        if (enabled) {
            // 如果出现Mapper.xml文件被占用的问题, 此处单独创建线程执行、
            Thread.currentThread().setName("MyBatis-Mapper-Refresh");
            try {
                if (location == null) {
                    location = new HashSet<>();
                    for (Resource mapperLocation : mapperLocations) {
                        String realFilePath = mapperLocation.toString().replaceAll("\\\\", "/");
                        realFilePath = realFilePath.substring("file [".length(), realFilePath.lastIndexOf("]"));
                        location.add(realFilePath);
                    }
                }

                TimeUnit.SECONDS.sleep(delaySeconds);

                System.out.println("========= Refreshable Mybatis Mapper =========");

                while (true) {
                    for (String path : location) {
                        refresh(path);
                    }
                    TimeUnit.SECONDS.sleep(intervalSeconds);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 执行刷新(核心方法)、
     *
     * @param filePath 刷新目录
     * @throws NestedIOException 解析异常
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void refresh(String filePath) throws Exception {
        // 本次刷新时间
        Long refreshTime = System.currentTimeMillis();

        // 1、获取需要刷新的Mapper文件列表
        List<File> fileList = this.getRefreshFile(new File(filePath));
        for (File file : fileList) {
            InputStream inputStream = new FileInputStream(file);
            String resource = file.getAbsolutePath();
            try {
                // 2、修改Configuration的字段类型, 更新为自己的StrictMap, 方便重新加载
                String[] mapFieldNames = new String[]{"mappedStatements", "caches",
                        "resultMaps", "parameterMaps", "keyGenerators", "sqlFragments"};
                for (String fieldName : mapFieldNames) {
                    Field field = configuration.getClass().getDeclaredField(fieldName);
                    Map<String, Object> map = getFieldValue(field, configuration);
                    if (!(map instanceof StrictMap)) {
                        Map newMap = new StrictMap(StringUtils.capitalize(fieldName) + "collection");
                        for (String key : map.keySet()) {
                            try {
                                newMap.put(key, map.get(key));
                            } catch (IllegalArgumentException ex) {
                                newMap.put(key, ex.getMessage());
                            }
                        }
                        setFieldValue(field, configuration, newMap);
                    }
                }

                // 3、清理已加载的资源标识,以便重新加载、
                Field loadedResourcesField = configuration.getClass().getDeclaredField("loadedResources");
                Set<String> loadedResourcesSet = getFieldValue(loadedResourcesField, configuration);
                loadedResourcesSet.remove(resource);

                // 4、重新编译加载资源文件、
                XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(inputStream, configuration,
                        resource, configuration.getSqlFragments());
                xmlMapperBuilder.parse();
            } catch (Exception e) {
                throw new NestedIOException("Failed to parse mapping resource: '" + resource + "'", e);
            } finally {
                ErrorContext.instance().reset();
            }
            if (log.isDebugEnabled()) {
                log.debug("Refresh File：" + file.getAbsolutePath());
            }
        }
        // 如果刷新了文件,则修改刷新时间,否则不修改
        if (fileList.size() > 0) {
            lastRefreshTime = refreshTime;
        }
    }

    /**
     * 获取需要刷新的文件列表
     *
     * @param file 目录/文件
     * @return 刷新文件列表
     */
    private List<File> getRefreshFile(File file) {
        List<File> fileList = new ArrayList<>();
        if (file.isFile()) {
            if (file.getName().contains(mapperSuffix)
                    && this.isNeedRefresh(file)) {
                fileList.add(file);
            }
        } else {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        fileList.addAll(this.getRefreshFile(f));
                    } else {
                        if (f.getName().contains(mapperSuffix)
                                && this.isNeedRefresh(f)) {
                            fileList.add(f);
                        }
                    }
                }
            }
        }
        return fileList;
    }

    /**
     * 判断文件是否需要刷新
     *
     * @param file 文件
     * @return 需要刷新返回true，否则返回false
     */
    private boolean isNeedRefresh(File file) {
        return file.lastModified() > lastRefreshTime;
    }

    /**
     * 重写 {@link Configuration.StrictMap} 类
     * 来自 MyBatis3.4.0版本，修改 put 方法，允许反复 put更新。
     */
    public static class StrictMap<V> extends HashMap<String, V> {

        private static final long serialVersionUID = -4950446264854982944L;
        private String name;

        public StrictMap(String name, int initialCapacity, float loadFactor) {
            super(initialCapacity, loadFactor);
            this.name = name;
        }

        public StrictMap(String name, int initialCapacity) {
            super(initialCapacity);
            this.name = name;
        }

        public StrictMap(String name) {
            super();
            this.name = name;
        }

        public StrictMap(String name, Map<String, ? extends V> m) {
            super(m);
            this.name = name;
        }

        @Override
        @SuppressWarnings("unchecked")
        public V put(String key, V value) {
            // 相较于org.apache.ibatis.session.Configuration.StrictMap#put(String,V)方法、
            // 此处判断删除操作、
            if (MapperFileRefresh.isRefreshable()) {
                remove(key);
                MapperFileRefresh.log.debug("Refresh StatementId：" + key);
            }
            if (containsKey(key)) {
                throw new IllegalArgumentException(name + " already contains value for " + key);
            } else {
                if (key.contains(".")) {
                    final String shortKey = getShortName(key);
                    if (super.get(shortKey) == null) {
                        super.put(shortKey, value);
                    } else {
                        super.put(shortKey, (V) new Ambiguity(shortKey));
                    }
                }
                return super.put(key, value);
            }
        }

        @Override
        public V get(Object key) {
            V value = super.get(key);
            if (value == null) {
                throw new IllegalArgumentException(name + " does not contain value for " + key);
            }
            if (value instanceof Ambiguity) {
                throw new IllegalArgumentException(((Ambiguity) value).getSubject() + " is ambiguous in " + name
                        + " (try using the full name including the namespace, or rename one of the entries)");
            }
            return value;
        }

        private String getShortName(String key) {
            final String[] keyParts = key.split("\\.");
            return keyParts[keyParts.length - 1];
        }

        protected static class Ambiguity {
            private String subject;

            public Ambiguity(String subject) {
                this.subject = subject;
            }

            public String getSubject() {
                return subject;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MapperFileRefresh that = (MapperFileRefresh) o;
        return location.equals(that.location) && Arrays.equals(mapperLocations, that.mapperLocations) && configuration.equals(that.configuration) && lastRefreshTime.equals(that.lastRefreshTime);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(location, configuration, lastRefreshTime);
        result = 31 * result + Arrays.hashCode(mapperLocations);
        return result;
    }

    private void setAccessible(Field field) {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
    }

    private void setInaccessible(Field field) {
        if (!field.isAccessible()) {
            field.setAccessible(false);
        }
    }

    @SuppressWarnings("unchecked")
    private <V> V getFieldValue(Field field, Object obj) throws IllegalAccessException {
        setAccessible(field);
        Object value = field.get(obj);
        setInaccessible(field);
        return (V) value;
    }

    private void setFieldValue(Field field, Object obj, Object value) throws IllegalAccessException {
        setAccessible(field);
        field.set(obj, value);
        setInaccessible(field);
    }
}
