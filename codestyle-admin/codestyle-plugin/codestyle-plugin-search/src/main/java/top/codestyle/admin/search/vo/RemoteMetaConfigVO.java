/*
 * Copyright (c) 2022-present Charles7c Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this fileInfo except in compliance with the License.
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

package top.codestyle.admin.search.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.codestyle.admin.search.model.es.entity.RemoteMetaDoc;

/**
 * @author ChonghaoGao
 * @date 2025/12/1 19:07)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemoteMetaConfigVO {

    /**
     * 组织名(用户名)
     */
    private String groupId;

    /**
     * 模板组名
     */
    private String artifactId;

    /**
     * 模板组总体描述
     */
    private String description;

    /**
     * 单个版本配置对象
     */
    private RemoteMetaDoc.Config config;

}
