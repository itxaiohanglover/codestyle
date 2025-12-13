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

package top.codestyle.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.codestyle.entity.es.vo.McpSearchResultVO;
import top.codestyle.service.McpSearchService;

import java.io.File;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/mcp")
@AllArgsConstructor
public class McpSearchController {

    private final McpSearchService mcpSearchService;

    /**
     * MCP模板检索接口
     * 
     * @param templateKeyword 搜索关键词
     * @return 模板搜索结果
     */
    @GetMapping("/search")
    public ResponseEntity<McpSearchResultVO> searchMcp(@RequestParam String templateKeyword) {
        log.info("进行mcp模板检索 - 关键词: {}", templateKeyword);
        McpSearchResultVO result = mcpSearchService.searchMcp(templateKeyword);
        return result != null ? ResponseEntity.ok(result) : ResponseEntity.ok(null);
    }

    /**
     * 上传meta.json文件并保存到Elasticsearch
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadMetaJson(@RequestParam("file") MultipartFile file) {
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("请选择要上传的文件");
            }

            // 检查文件类型
            if (!file.getOriginalFilename().equals("meta.json")) {
                return ResponseEntity.badRequest().body("文件必须名为meta.json");
            }

            // 保存临时文件
            File tempFile = File.createTempFile("meta", ".json");
            file.transferTo(tempFile);

            try {
                // 调用服务保存到Elasticsearch
                boolean result = mcpSearchService.saveMetaJsonToElasticsearch(tempFile);
                if (result) {
                    return ResponseEntity.ok("meta.json文件保存成功");
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("meta.json文件保存失败");
                }
            } finally {
                // 删除临时文件
                tempFile.delete();
            }
        } catch (IOException e) {
            log.error("上传meta.json文件失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("上传文件失败: " + e.getMessage());
        }
    }
}