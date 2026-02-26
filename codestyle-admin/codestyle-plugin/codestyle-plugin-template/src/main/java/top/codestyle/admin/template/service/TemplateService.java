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

package top.codestyle.admin.template.service;

import top.codestyle.admin.template.model.query.TemplateQuery;
import top.codestyle.admin.template.model.req.TemplateReq;
import top.codestyle.admin.template.model.resp.TemplateDetailResp;
import top.codestyle.admin.template.model.resp.TemplateItemResp;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;

import java.util.List;

public interface TemplateService {

    PageResp<TemplateItemResp> listTemplates(TemplateQuery query, PageQuery pageQuery);

    PageResp<TemplateItemResp> listFavorites(TemplateQuery query, PageQuery pageQuery);

    PageResp<TemplateItemResp> listMyTemplates(TemplateQuery query, PageQuery pageQuery);

    void toggleVisibility(Long id);

    TemplateDetailResp getTemplateDetail(Long id);

    Boolean toggleFavorite(Long templateId);

    void incrementDownloadCount(Long id);

    Long saveSnippetToLibrary(Long snippetId, String name, String description, List<?> tags);

    Long create(TemplateReq req);

    void update(Long id, TemplateReq req);

    void delete(List<Long> ids);

    List<TemplateItemResp> listVersions(String groupId, String artifactId);
}
