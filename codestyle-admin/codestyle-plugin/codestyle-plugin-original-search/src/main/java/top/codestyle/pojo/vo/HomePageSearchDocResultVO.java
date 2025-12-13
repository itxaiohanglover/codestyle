/*
 * Copyright (c) 2022-present Charles7c Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.codestyle.pojo.vo;

/**
 * @author ChonghaoGao
 * @date 2025/11/14 08:43
 *       用于提供首页
 */

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 首页搜索单条结果 VO
 */
@Data
public class HomePageSearchDocResultVO {

    private String Id;
    // 模板中文名称
    private String nameCh;
    // 模板英文名称
    private String nameEn;
    // 模板描述
    private String description;
    // 模板图标
    private String templateAvatar;
    // 模板标签 —— 模板内部的自分类
    private List<String> tags;

    // 模板创建者的名称
    private String creatorName;
    // 模板成员名称
    private List<String> memberName;
    // 模板成员Urls
    private List<String> memberAvatarUrls;

    //模板当前的版本
    private String version;

    // 模板创建时间
    private Date createTime;
    // 模板最近一次编辑更新时间
    private Date updateTime;
    // 模板 编辑时间
    private Date editTime;

    // 点赞数
    private Long totalLikeCount;
    // 收藏数量
    private Long totalFavoriteCount;

}
