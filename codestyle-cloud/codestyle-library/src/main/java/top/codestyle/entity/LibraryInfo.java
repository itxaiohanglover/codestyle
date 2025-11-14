package top.codestyle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 模板库信息实体
 *
 * @author huxc2020
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("library_info")
public class LibraryInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模板库英文名
     */
    @TableField("nameEn")
    private String nameEn;

    /**
     * 模板库中文名
     */
    @TableField("nameCh")
    private String nameCh;

    /**
     * 模板库描述
     */
    private String description;

    /**
     * 模板库图片介绍
     */
    private String avatar;

    /**
     * 状态：0：私有；1：公有
     */
    private Integer status;

    /**
     * 模版库的最大总大小 单位是字节(B)
     */
    @TableField("maxFileSize")
    private Long maxFileSize;

    /**
     * 模版库文件的最大数量
     */
    @TableField("maxFileCount")
    private Long maxFileCount;

    /**
     * 模版库成员的最大数量
     */
    @TableField("maxMemberCount")
    private Long maxMemberCount;

    /**
     * 模版库当前的总大小 单位是字节(B)
     */
    @TableField("totalFileSize")
    private Long totalFileSize;

    /**
     * 模版库当前的文件数量
     */
    @TableField("totalFileCount")
    private Long totalFileCount;

    /**
     * 模版库当前的成员数量
     */
    @TableField("totalMemberCount")
    private Long totalMemberCount;

    /**
     * 模板库当前的点赞数
     */
    @TableField("totalLikeCount")
    private Long totalLikeCount;

    /**
     * 模板库当前的收藏数
     */
    @TableField("totalFavoriteCount")
    private Long totalFavoriteCount;

    /**
     * 编辑时间
     */
    @TableField("editTime")
    private Date editTime;

    /**
     * 创建时间
     */
    @TableField("createTime")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("updateTime")
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    @TableField("isDelete")
    private Integer isDelete;
}

