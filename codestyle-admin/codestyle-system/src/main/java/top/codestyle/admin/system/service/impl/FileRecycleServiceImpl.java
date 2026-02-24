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

package top.codestyle.admin.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.plugins.IgnoreStrategy;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.codestyle.admin.common.context.UserContextHolder;
import top.codestyle.admin.system.mapper.FileMapper;
import top.codestyle.admin.system.model.entity.FileDO;
import top.codestyle.admin.system.model.entity.StorageDO;
import top.codestyle.admin.system.model.query.FileQuery;
import top.codestyle.admin.system.model.resp.file.FileResp;
import top.codestyle.admin.system.service.FileRecycleService;
import top.codestyle.admin.system.service.StorageService;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.exception.BusinessException;
import top.continew.starter.data.util.QueryWrapperHelper;
import top.continew.starter.extension.crud.model.query.PageQuery;
import top.continew.starter.extension.crud.model.resp.PageResp;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文件回收站业务实现
 *
 * @author Charles7c
 * @since 2025/11/11 21:28
 */
@Service
@RequiredArgsConstructor
public class FileRecycleServiceImpl implements FileRecycleService {

    private final FileMapper fileMapper;
    private final StorageService storageService;
    private final FileStorageService fileStorageService;

    @Override
    public PageResp<FileResp> page(FileQuery query, PageQuery pageQuery) {
        QueryWrapper<FileDO> queryWrapper = QueryWrapperHelper.build(query, pageQuery.getSort());
        Page<FileDO> page = fileMapper.selectPageInRecycleBin(new Page<>(pageQuery.getPage(), pageQuery
            .getSize()), queryWrapper.lambda().eq(FileDO::getDeleted, 1L));
        return PageResp.build(page, FileResp.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restore(Long id) {
        FileDO file = this.getById(id);
        // 恢复记录
        fileMapper.restoreInRecycleBin(id, UserContextHolder.getUserId());
        // 还原文件
        StorageDO storage = storageService.getById(file.getStorageId());
        FileInfo fileInfo = file.toFileInfo(storage);
        fileInfo.setPath(storage.getRecycleBinPath() + fileInfo.getPath());
        String newPath = fileInfo.getPath().replace(storage.getRecycleBinPath(), StringConstants.EMPTY);
        fileStorageService.move(fileInfo).setPath(newPath).move();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        FileDO file = this.getById(id);
        // 删除记录
        fileMapper.deleteWithoutRecycleBin(List.of(id), UserContextHolder.getUserId());
        // 删除文件
        StorageDO storage = storageService.getById(file.getStorageId());
        FileInfo fileInfo = file.toFileInfo(storage);
        fileInfo.setPath(storage.getRecycleBinPath() + fileInfo.getPath());
        fileStorageService.delete(fileInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clean() {
        // 查询回收站记录
        List<FileDO> list = fileMapper.selectListInRecycleBin();
        if (CollUtil.isEmpty(list)) {
            return;
        }
        try {
            InterceptorIgnoreHelper.handle(IgnoreStrategy.builder().blockAttack(true).build());
            // 删除记录
            fileMapper.cleanRecycleBin(UserContextHolder.getUserId());
            // 删除文件
            // 批量获取存储配置
            Map<Long, List<FileDO>> fileListGroup = list.stream().collect(Collectors.groupingBy(FileDO::getStorageId));
            List<StorageDO> storageList = storageService.listByIds(fileListGroup.keySet());
            Map<Long, StorageDO> storageGroup = storageList.stream()
                .collect(Collectors.toMap(StorageDO::getId, Function.identity(), (existing, replacement) -> existing));
            // 删除文件
            for (Map.Entry<Long, List<FileDO>> entry : fileListGroup.entrySet()) {
                StorageDO storage = storageGroup.get(entry.getKey());
                // 清空回收站
                FileInfo fileInfo = new FileInfo();
                fileInfo.setPlatform(storage.getCode());
                fileInfo.setBasePath(StringConstants.EMPTY);
                fileInfo.setPath(storage.getRecycleBinPath());
                fileStorageService.delete(fileInfo);
            }
        } finally {
            InterceptorIgnoreHelper.clearIgnoreStrategy();
        }
    }

    /**
     * 根据 ID 查询
     *
     * @param id ID
     * @return 文件信息
     */
    private FileDO getById(Long id) {
        FileDO file = fileMapper.selectByIdInRecycleBin(id);
        if (file == null) {
            throw new BusinessException("ID 为 [%s] 的文件已不存在".formatted(id));
        }
        return file;
    }
}
