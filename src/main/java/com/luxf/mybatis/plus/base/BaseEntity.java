package com.luxf.mybatis.plus.base;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author 小66
 * @create 2020-12-15 20:37
 **/
@Data
public abstract class BaseEntity {

    @TableId("ID")
    private Long id;

    private String createdBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;
}
