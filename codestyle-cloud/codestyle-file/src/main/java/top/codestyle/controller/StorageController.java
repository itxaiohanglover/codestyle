/*
 * Copyright (c) 2025-present IPBD Organization. All Rights Reserved.
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

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import top.codestyle.service.StorageService;
import top.codestyle.model.dto.file.StorageReq;
import top.codestyle.model.query.StorageQuery;
import top.codestyle.model.vo.StorageResp;
import top.continew.starter.extension.crud.annotation.CrudRequestMapping;
import top.continew.starter.extension.crud.enums.Api;


/**
 * 存储管理 API
 *
 * @author GALAwang
 * @since 2023/12/26 22:09
 */
@Tag(name = "存储管理 API")
@Validated
@RestController
@CrudRequestMapping(value = "/system/storage", api = {Api.PAGE, Api.DETAIL, Api.ADD, Api.UPDATE, Api.DELETE})
public class StorageController extends BaseController<StorageService, StorageResp, StorageResp, StorageQuery, StorageReq> {
}