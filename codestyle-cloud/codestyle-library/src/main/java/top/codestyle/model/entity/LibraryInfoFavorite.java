package top.codestyle.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.sql.Date;

@Data
@TableName("library_info_favorite")
public class LibraryInfoFavorite implements Serializable {
    /**
     * 主键
     */

    private Long id;
    /**
     * 模板库ID
     */

    private Long infoId;
    /**
     * 用户ID
     */

    private Long userId;


    /**
     * 创建时间
     */
    private Date createtime;
}
