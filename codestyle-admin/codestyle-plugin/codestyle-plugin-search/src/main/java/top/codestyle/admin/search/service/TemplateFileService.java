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

package top.codestyle.admin.search.service;

import org.springframework.web.multipart.MultipartFile;
import top.codestyle.admin.search.model.resp.TemplateUploadResp;

import java.io.IOException;

/**
 * 模板文件服务接口
 *
 * @author CodeStyle Team
 * @since 1.0.0
 */
public interface TemplateFileService {

    /**
     * 上传模板
     *
     * @param file       ZIP 文件
     * @param groupId    组ID
     * @param artifactId 项目ID
     * @param version    版本号
     * @return 模板信息
     * @throws IOException IO 异常
     */
    TemplateUploadResp uploadTemplate(
        MultipartFile file,
        String groupId,
        String artifactId,
        String version
    ) throws IOException;
}

