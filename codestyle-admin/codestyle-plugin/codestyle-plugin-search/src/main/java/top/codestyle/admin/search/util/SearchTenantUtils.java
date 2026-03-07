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

package top.codestyle.admin.search.util;

import lombok.extern.slf4j.Slf4j;
import top.codestyle.admin.common.context.ApiTenantContextHolder;
import top.codestyle.admin.search.model.SearchRequest;
import top.continew.starter.extension.tenant.context.TenantContextHolder;

/**
 * 检索模块租户工具
 *
 * @author CodeStyle Team
 * @since 1.0.0
 */
@Slf4j
public final class SearchTenantUtils {

    private SearchTenantUtils() {
    }

    public static Long resolveSearchTenantId(SearchRequest request) {
        Long tenantId = request.getTenantId();
        Long effectiveTenantId = tenantId != null ? tenantId : 0L;
        log.info("ES 租户解析 request.tenantId={}, effectiveTenantId={}", tenantId, effectiveTenantId);
        return effectiveTenantId;
    }

    public static Long resolveCurrentTenantId() {
        Long apiTenantId = ApiTenantContextHolder.getTenantId();
        if (apiTenantId != null) {
            log.info("模板租户解析从 ApiTenantContextHolder 获取 tenantId={}", apiTenantId);
            return apiTenantId;
        }
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId != null) {
            log.info("模板租户解析从 TenantContextHolder 获取 tenantId={}", tenantId);
            return tenantId;
        }
        log.info("模板租户解析使用默认 tenantId=0");
        return 0L;
    }
}
