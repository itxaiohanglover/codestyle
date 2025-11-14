package top.codestyle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 模板人员关系实体
 *
 * @author huxc2020
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("library_member")
public class LibraryMember implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模板库ID
     */
    @TableField("library_id")
    private Long libraryId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 角色：1=owner 2=admin 3=normal
     */
    private Integer role;

    /**
     * 加入时间
     */
    @TableField("joined_at")
    private Date joinedAt;
}

