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
 * vip实体
 *
 * @author huxc2020
 */

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("vip_user")
public class VipPo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * vip编号
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * vip用户ID
     */
    @TableField("vip_user")
    private Long vipUser;
    
    /**
     * 过期时间
     */
    @TableField("expired_time")
    private Date expiredTime;
}
