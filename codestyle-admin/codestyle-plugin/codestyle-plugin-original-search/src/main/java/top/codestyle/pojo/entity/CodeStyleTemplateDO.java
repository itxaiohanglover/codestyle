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

package top.codestyle.pojo.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import java.util.Date;
import java.util.List;

@Data
@Document(indexName = "template_index")
@Setting(settingPath = "/elasticsearch/template-settings.json")
public class CodeStyleTemplateDO {

    // ===================== 主键 =====================
    @Id
    private String id;

    // ===================== 搜索核心字段 =====================

    /**
     * 英文名称：一般不需要拼音、中文特性，使用英文检索即可
     */
    @Field(type = FieldType.Text, analyzer = "en_std", searchAnalyzer = "en_std")
    private String nameEn;

    /**
     * 中文名称：最重要的搜索字段
     * 使用中文 + 拼音混合检索
     */
    @MultiField(mainField = @Field(type = FieldType.Text, analyzer = "cn_en_index", searchAnalyzer = "cn_en_search"))
    private String nameCh;

    /**
     * 中文描述：适合拼音检索，并提供 keyword 用于排序/过滤
     */
    @MultiField(mainField = @Field(type = FieldType.Text, analyzer = "cn_en_index", searchAnalyzer = "cn_en_search"), otherFields = {
        @InnerField(type = FieldType.Keyword, suffix = "keyword")})
    private String description;

    /**
     * 搜索标签：建议使用 keyword + text 多字段
     * 既能全文检索，又能精确匹配过滤
     */
    @MultiField(mainField = @Field(type = FieldType.Text, analyzer = "cn_en_index", searchAnalyzer = "cn_en_search"), otherFields = {
        @InnerField(type = FieldType.Keyword, suffix = "keyword")})
    private List<String> searchTags;

    // ===================== 展示类字段（无需分词） =====================

    @Field(type = FieldType.Keyword)
    private String version; // 版本信息

    @Field(type = FieldType.Keyword)
    private String avatar; // 空间图像 - 封面

    @Field(type = FieldType.Keyword)
    private List<String> tagsAgg; // 聚合得到的类别

    @Field(type = FieldType.Keyword)
    private List<String> memberNames;

    @Field(type = FieldType.Keyword)
    private List<String> memberAvatarUrls;

    // ===================== 非展示类字段（无需分词） ===================== 未来可以做一个链接到创建者和其它成员的个人主页这些
    @Field(type = FieldType.Keyword)
    private Long creatorId;

    @Field(type = FieldType.Keyword)
    private String creatorName;

    @Field(type = FieldType.Keyword)
    private List<Long> memberIds;

    // ===================== 统计字段（数值类型） =====================

    @Field(type = FieldType.Integer)
    private Integer isPrivate; // 1代表私有 0代表公开

    @Field(type = FieldType.Long)
    private Long totalFileSize;

    @Field(type = FieldType.Long)
    private Long totalFileCount;

    @Field(type = FieldType.Long)
    private Long totalMemberCount;

    @Field(type = FieldType.Long)
    private Long totalLikeCount;

    @Field(type = FieldType.Long)
    private Long totalFavoriteCount;

    // ===================== 时间字段 =====================
    @Field(type = FieldType.Date)
    private Date createTime;

    @Field(type = FieldType.Date)
    private Date updateTime;

    @Field(type = FieldType.Date)
    private Date editTime;

    // ===================== 逻辑删除 =====================
    @Field(type = FieldType.Integer)
    private Integer isDelete;

    // 人为操作分数的逻辑
    @Field(type = FieldType.Double)
    private Double hotScoreWeight;

}
