package com.luxf.mybatis.plus.config.refresh;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 自定义的配置、可以在application.yml中配置相关内容、
 *
 * @author 小66
 * @date 2020-08-13 11:29
 **/
@ConfigurationProperties(
        prefix = "custom.mybatis.refresh"
)
public class MybatisRefreshProperties {
    /**
     * 是否启用Mapper.xml刷新功能
     */
    private Boolean enabled = false;
    /**
     * 延迟刷新
     */
    private Integer delaySeconds = 5;
    /**
     * 间隔时间
     */
    private Integer intervalSeconds = 3;

    /**
     * mapper文件的后缀名、用于匹配是否是的mapper文件、
     */
    private String mapperSuffix = "Mapper.xml";

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getDelaySeconds() {
        return delaySeconds;
    }

    public void setDelaySeconds(Integer delaySeconds) {
        this.delaySeconds = delaySeconds;
    }

    public Integer getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(Integer intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }

    public String getMapperSuffix() {
        return mapperSuffix;
    }

    public void setMapperSuffix(String mapperSuffix) {
        this.mapperSuffix = mapperSuffix;
    }
}
