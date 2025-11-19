package top.codestyle.model.entity;



import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;


import java.io.Serializable;

import java.util.Date;


/**
* 模板库评论
*/
@Data
@Builder
@TableName("library_info_comment")
public class LibraryInfoComment implements Serializable {

    /**
    * 评论主键
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
    * 0=一级评论；>0=回复目标评论ID
    */

    private Long parentId;
    /**
    * 评论内容
    */

    private String content;
    /**
    * 子回复数
    */

    private Integer replyCnt;
    /**
    * 软删除
    */

    private Integer isDeleted;
    /**
    * 
    */

    private Date createtime;
    /**
    * 
    */

    private Date updatetime;


}
