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

package top.codestyle.admin.system.enums;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.continew.starter.core.enums.BaseEnum;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 文件类型枚举
 *
 * @author Charles7c
 * @since 2023/12/23 13:38
 */
@Getter
@RequiredArgsConstructor
public enum FileTypeEnum implements BaseEnum<Integer> {

    /**
     * 目录
     */
    DIR(0, "目录", Collections.emptyList()),

    /**
     * 其他
     */
    UNKNOWN(1, "其他", Collections.emptyList()),

    /**
     * 图片
     */
    IMAGE(2, "图片", List
        .of("jpg", "jpeg", "png", "gif", "bmp", "webp", "ico", "psd", "tiff", "dwg", "jxr", "apng", "xcf")),

    /**
     * 文档
     */
    DOC(3, "文档", List.of("txt", "pdf", "doc", "xls", "ppt", "docx", "xlsx", "pptx")),

    /**
     * 视频
     */
    VIDEO(4, "视频", List.of("mp4", "avi", "mkv", "flv", "webm", "wmv", "m4v", "mov", "mpg", "rmvb", "3gp")),

    /**
     * 音频
     */
    AUDIO(5, "音频", List.of("mp3", "flac", "wav", "ogg", "midi", "m4a", "aac", "amr", "ac3", "aiff")),

    /**
     * 模板文件
     */
    TEMPLATE(6, "模板", List.of("ftl", "vm", "tpl")),

    /**
     * 代码文件
     */
    CODE(7, "代码", List.of(
        "java", "py", "js", "ts", "go", "rs", "c", "cpp", "h", "hpp",
        "cs", "php", "rb", "swift", "kt", "scala", "sh", "bat", "ps1"
    )),

    /**
     * 配置文件
     */
    CONFIG(8, "配置", List.of("yml", "yaml", "xml", "json", "properties", "conf", "ini", "toml")),

    /**
     * 文档文件
     */
    MARKDOWN(9, "文档", List.of("md", "markdown", "rst", "adoc")),;

    private final Integer value;
    private final String description;
    private final List<String> extensions;

    /**
     * 根据扩展名查询
     *
     * @param extension 扩展名
     * @return 文件类型
     */
    public static FileTypeEnum getByExtension(String extension) {
        return Arrays.stream(FileTypeEnum.values())
            .filter(t -> t.getExtensions().contains(StrUtil.emptyIfNull(extension).toLowerCase()))
            .findFirst()
            .orElse(FileTypeEnum.UNKNOWN);
    }

    /**
     * 获取所有扩展名
     *
     * @return 所有扩展名
     */
    public static List<String> getAllExtensions() {
        return Arrays.stream(FileTypeEnum.values()).flatMap(t -> t.getExtensions().stream()).toList();
    }
}
