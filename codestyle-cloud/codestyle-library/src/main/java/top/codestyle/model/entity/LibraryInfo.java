package top.codestyle.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Date;

/**
* 模板库主表
* @TableName library_info
*/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("library_info")
public class LibraryInfo implements Serializable {

    /**
    * id
    */

    private Long id;
    /**
    * 模板库英文名
    */

    private String nameen;
    /**
    * 模板库中文名
    */

    private String namech;
    /**
    * 模板库描述
    */

    private String description;
    /**
    * 模板库图片介绍
    */

    private String avatar;
    /**
    * 状态：0-私有 1-公有
    */

    private Integer status;
    /**
    * 模版库最大总大小(字节)
    */

    private Long maxfilesize;
    /**
    * 模版库文件最大数量
    */

    private Long maxfilecount;
    /**
    * 模版库成员最大数量
    */

    private Long maxmembercount;
    /**
    * 当前总大小(字节)
    */

    private Long totalfilesize;
    /**
    * 当前文件数量
    */

    private Long totalfilecount;
    /**
    * 当前成员数量
    */
    private Long totalmembercount;
    /**
    * 当前点赞数
    */

    private Long totallikecount;
    /**
    * 当前收藏数
    */

    private Long totalfavoritecount;
    /**
    * 编辑时间
    */

    private Date edittime;
    /**
    * 创建时间
    */

    private Date createtime;
    /**
    * 更新时间
    */

    private Date updatetime;
    /**
    * 是否删除
    */

    private Integer isdelete;



}
