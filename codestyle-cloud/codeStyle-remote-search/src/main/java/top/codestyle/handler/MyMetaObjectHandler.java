package top.codestyle.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * MyBatis-Plus字段自动填充处理器
 * 用于自动填充create_time和update_time字段
 *
 * @author ChonghaoGao
 * @date 2025/12/4
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入操作时自动填充
     * 填充create_time、update_time和deleted字段
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("开始执行插入操作的字段自动填充");
        // 使用非严格模式填充create_time字段
        this.setFieldValByName("createTime", new Date(), metaObject);
        // 使用非严格模式填充update_time字段
        this.setFieldValByName("updateTime", new Date(), metaObject);
        // 使用非严格模式填充deleted字段，默认值为0（未删除）
        this.setFieldValByName("deleted", 0, metaObject);
    }

    /**
     * 更新操作时自动填充
     * 填充update_time字段
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("开始执行更新操作的字段自动填充");
        // 使用非严格模式填充update_time字段
        this.setFieldValByName("updateTime", new Date(), metaObject);
    }
}
