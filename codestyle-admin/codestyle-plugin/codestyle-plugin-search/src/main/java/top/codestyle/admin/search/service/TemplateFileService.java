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

package top.codestyle.admin.search.service;

import org.springframework.web.multipart.MultipartFile;
import top.codestyle.admin.search.model.resp.TemplateUploadResp;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 模板文件服务接口
 * <p>
 * 负责模板压缩包的上传解压、文件存储、文件树查询、内容预览与下载打包。
 * 所有文件通过系统 FileService 存储，可在文件管理器中可见。
 * </p>
 *
 * @author CodeStyle Team
 * @since 1.0.0
 */
public interface TemplateFileService {

    /**
     * 上传并解压模板压缩包
     * <p>
     * 支持 ZIP、TAR.GZ、7Z 格式。解压后逐个文件上传到系统文件管理服务，
     * 自动从 meta.json 中解析 groupId/artifactId/version 等参数。
     * </p>
     *
     * @param file       压缩包文件
     * @param groupId    组ID
     * @param artifactId 项目ID
     * @param version    版本号
     * @return 模板上传响应
     * @throws IOException IO 异常
     */
    TemplateUploadResp uploadTemplate(MultipartFile file,
                                      String groupId,
                                      String artifactId,
                                      String version,
                                      Boolean overwrite) throws IOException;

    /**
     * 获取模板文件树
     * <p>
     * 从系统文件表中查询指定模板版本下的所有文件和目录，
     * 构建树形结构返回。
     * </p>
     *
     * @param groupId    组ID
     * @param artifactId 项目ID
     * @param version    版本号
     * @return 文件树（含 name、path、isDir、children、size 等字段）
     */
    List<Map<String, Object>> listFiles(String groupId, String artifactId, String version);

    /**
     * 读取模板文件内容
     * <p>
     * 从存储系统中下载指定模板文件，以 UTF-8 文本返回。
     * 文件大小不得超过 512KB。
     * </p>
     *
     * @param groupId    组ID
     * @param artifactId 项目ID
     * @param version    版本号
     * @param filePath   模板内相对路径
     * @return 文件文本内容
     * @throws IOException IO 异常
     */
    String readFileContent(String groupId, String artifactId, String version, String filePath) throws IOException;

    /**
     * 将模板打包为 ZIP 临时文件
     * <p>
     * 从存储系统中下载指定模板版本的所有文件，
     * 按原始目录结构打包到临时 ZIP 文件中。调用方负责删除临时文件。
     * </p>
     *
     * @param groupId    组ID
     * @param artifactId 项目ID
     * @param version    版本号
     * @return 打包好的临时 ZIP 文件
     * @throws IOException IO 异常
     */
    File createTemplateZip(String groupId, String artifactId, String version) throws IOException;

    void deleteTemplateFiles(String groupId, String artifactId, String version);
}
