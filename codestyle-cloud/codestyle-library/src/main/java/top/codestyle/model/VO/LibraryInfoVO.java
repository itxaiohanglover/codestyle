package top.codestyle.model.VO;

import lombok.Data;

import java.sql.Date;

@Data
public class LibraryInfoVO {

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
     * 是否已点赞
     */
    private Boolean hasLike;

    /**
     * 是否已收藏
     */
    private Boolean hasFavorite;
}
