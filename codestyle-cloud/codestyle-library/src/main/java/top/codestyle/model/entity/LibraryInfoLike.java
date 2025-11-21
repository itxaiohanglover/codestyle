package top.codestyle.model.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.sql.Date;

/**
* 用户点赞模板库
* @TableName library_info_like
*/
@Data
@TableName("library_info_like")
public class LibraryInfoLike implements Serializable {

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
