/*
 * Copyright (c) 2022-present CodeStyle Authors. All Rights Reserved.
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

package top.codestyle.admin.template.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.codestyle.admin.common.context.UserContextHolder;
import top.codestyle.admin.template.mapper.*;
import top.codestyle.admin.template.model.entity.*;
import top.codestyle.admin.template.model.query.TemplateQuery;
import top.codestyle.admin.template.model.resp.*;
import top.codestyle.admin.template.service.TemplateService;
import top.continew.starter.core.util.validation.CheckUtils;
import top.continew.starter.data.util.QueryWrapperHelper;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private final TemplateMapper templateMapper;
    private final TemplateTagMapper templateTagMapper;
    private final TemplateFavoriteMapper templateFavoriteMapper;
    private final CodeSnippetMapper codeSnippetMapper;
    private final ObjectMapper objectMapper;

    @Override
    public PageResp<TemplateItemResp> listTemplates(TemplateQuery query, PageQuery pageQuery) {
        QueryWrapper<TemplateDO> queryWrapper = QueryWrapperHelper.build(query);
        queryWrapper.orderByDesc("update_time");

        Page<TemplateDO> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        Page<TemplateDO> result = templateMapper.selectPage(page, queryWrapper);

        Long userId = null;
        try {
            userId = UserContextHolder.getUserId();
        } catch (Exception ignored) {
        }

        List<Long> templateIds = result.getRecords().stream().map(TemplateDO::getId).collect(Collectors.toList());
        Map<Long, List<TagItemResp>> tagMap = getTagsByTemplateIds(templateIds);
        Set<Long> favoriteIds = userId != null ? getFavoriteTemplateIds(userId, templateIds) : Collections.emptySet();

        List<TemplateItemResp> list = result.getRecords().stream().map(t -> {
            TemplateItemResp resp = BeanUtil.copyProperties(t, TemplateItemResp.class);
            resp.setTags(tagMap.getOrDefault(t.getId(), Collections.emptyList()));
            resp.setIsFavorite(favoriteIds.contains(t.getId()));
            return resp;
        }).collect(Collectors.toList());

        PageResp<TemplateItemResp> pageResp = new PageResp<>();
        pageResp.setList(list);
        pageResp.setTotal(result.getTotal());
        return pageResp;
    }

    @Override
    public TemplateDetailResp getTemplateDetail(Long id) {
        TemplateDO template = templateMapper.selectById(id);
        CheckUtils.throwIfNull(template, "模板不存在: {}", id);

        TemplateDetailResp resp = BeanUtil.copyProperties(template, TemplateDetailResp.class);

        List<TemplateTagDO> tags = templateTagMapper.selectList(new LambdaQueryWrapper<TemplateTagDO>()
            .eq(TemplateTagDO::getTemplateId, id));
        resp.setTags(tags.stream().map(t -> new TagItemResp(t.getLabel(), t.getColor())).collect(Collectors.toList()));

        Long userId = null;
        try {
            userId = UserContextHolder.getUserId();
        } catch (Exception ignored) {
        }
        if (userId != null) {
            Long count = templateFavoriteMapper.selectCount(new LambdaQueryWrapper<TemplateFavoriteDO>()
                .eq(TemplateFavoriteDO::getTemplateId, id)
                .eq(TemplateFavoriteDO::getUserId, userId));
            resp.setIsFavorite(count > 0);
        } else {
            resp.setIsFavorite(false);
        }

        Long starCount = templateFavoriteMapper.selectCount(new LambdaQueryWrapper<TemplateFavoriteDO>()
            .eq(TemplateFavoriteDO::getTemplateId, id));
        resp.setStarCount(starCount.intValue());

        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean toggleFavorite(Long templateId) {
        Long userId = UserContextHolder.getUserId();

        LambdaQueryWrapper<TemplateFavoriteDO> wrapper = new LambdaQueryWrapper<TemplateFavoriteDO>()
            .eq(TemplateFavoriteDO::getTemplateId, templateId)
            .eq(TemplateFavoriteDO::getUserId, userId);

        Long count = templateFavoriteMapper.selectCount(wrapper);
        if (count > 0) {
            templateFavoriteMapper.delete(wrapper);
            return false;
        } else {
            TemplateFavoriteDO fav = new TemplateFavoriteDO();
            fav.setTemplateId(templateId);
            fav.setUserId(userId);
            templateFavoriteMapper.insert(fav);
            return true;
        }
    }

    @Override
    public void incrementDownloadCount(Long id) {
        TemplateDO template = templateMapper.selectById(id);
        CheckUtils.throwIfNull(template, "模板不存在: {}", id);
        template.setDownloadCount(template.getDownloadCount() + 1);
        templateMapper.updateById(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveSnippetToLibrary(Long snippetId, String name, String description, List<?> tags) {
        CodeSnippetDO snippet = codeSnippetMapper.selectById(snippetId);
        CheckUtils.throwIfNull(snippet, "代码片段不存在: {}", snippetId);

        TemplateDO template = new TemplateDO();
        template.setName(StrUtil.isNotBlank(name) ? name : snippet.getLanguage().toUpperCase() + " 代码片段");
        template.setIcon(snippet.getLanguage().substring(0, Math.min(3, snippet.getLanguage().length())).toUpperCase());
        template.setAuthor("AI 生成");
        template.setDescription(StrUtil.isNotBlank(description) ? description : snippet.getContext());
        template.setVersion("v1.0.0");
        template.setDownloadCount(0);
        template.setRating(0.0);

        templateMapper.insert(template);

        if (tags != null) {
            try {
                for (Object tagObj : tags) {
                    Map<String, String> tagMap = objectMapper
                        .convertValue(tagObj, new TypeReference<Map<String, String>>() {});
                    TemplateTagDO tag = new TemplateTagDO();
                    tag.setTemplateId(template.getId());
                    tag.setLabel(tagMap.getOrDefault("label", ""));
                    tag.setColor(tagMap.getOrDefault("color", "blue"));
                    templateTagMapper.insert(tag);
                }
            } catch (Exception e) {
                log.warn("保存标签失败", e);
            }
        }

        return template.getId();
    }

    private Map<Long, List<TagItemResp>> getTagsByTemplateIds(List<Long> templateIds) {
        if (templateIds.isEmpty())
            return Collections.emptyMap();
        List<TemplateTagDO> tags = templateTagMapper.selectList(new LambdaQueryWrapper<TemplateTagDO>()
            .in(TemplateTagDO::getTemplateId, templateIds));
        return tags.stream()
            .collect(Collectors.groupingBy(TemplateTagDO::getTemplateId, Collectors.mapping(t -> new TagItemResp(t
                .getLabel(), t.getColor()), Collectors.toList())));
    }

    private Set<Long> getFavoriteTemplateIds(Long userId, List<Long> templateIds) {
        if (templateIds.isEmpty())
            return Collections.emptySet();
        List<TemplateFavoriteDO> favorites = templateFavoriteMapper
            .selectList(new LambdaQueryWrapper<TemplateFavoriteDO>().eq(TemplateFavoriteDO::getUserId, userId)
                .in(TemplateFavoriteDO::getTemplateId, templateIds));
        return favorites.stream().map(TemplateFavoriteDO::getTemplateId).collect(Collectors.toSet());
    }
}
