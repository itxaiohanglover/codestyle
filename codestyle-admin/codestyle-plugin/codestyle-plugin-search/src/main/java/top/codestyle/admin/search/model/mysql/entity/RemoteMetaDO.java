package top.codestyle.admin.search.model.mysql.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import top.codestyle.admin.common.base.model.entity.BaseDO;
import top.codestyle.admin.search.model.es.entity.RemoteMetaDoc;

import java.io.Serial;

/**
 * @author ChonghaoGao
 * @date 2025/12/1 19:38
 */
@Data
@TableName("remote_meta_info") // 直接使用完整表名，与数据库表名一致
public class RemoteMetaDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private Long id;

    // JSON字段，存储完整的meta信息
    private String metaJson;

    // 虚拟列，由数据库自动从meta_json中提取，不允许插入和更新
    @TableField(value = "group_id", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private String groupId;

//    // 软删除字段
//    @TableLogic // 逻辑删除注解
//    @TableField(fill = FieldFill.INSERT) // 插入时自动填充
//    private Integer deleted;

    // 解析metaJson为RemoteMetaDoc对象
    public RemoteMetaDoc toRemoteMetaDoc() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(metaJson, RemoteMetaDoc.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("解析metaJson失败: " + e.getMessage(), e);
        }
    }
}