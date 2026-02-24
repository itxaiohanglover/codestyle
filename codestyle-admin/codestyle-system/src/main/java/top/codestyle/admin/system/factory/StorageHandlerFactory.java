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

package top.codestyle.admin.system.factory;/*
                                            * Copyright (c) 2022-present Charles7c Authors. All Rights Reserved.
                                            *
                                            * Licensed under the Apache License, Version 2.0 (the "License");
                                            * you may not use this file except in compliance with the License.
                                            * You may obtain a copy of the License at
                                            *
                                            * http://www.apache.org/licenses/LICENSE-2.0
                                            *
                                            * Unless required by applicable law or agreed to in writing, software
                                            * distributed under the License is distributed on an "AS IS" BASIS,
                                            * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
                                            * See the License for the specific language governing permissions and
                                            * limitations under the License.
                                            */

import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.codestyle.admin.system.enums.StorageTypeEnum;
import top.codestyle.admin.system.handler.StorageHandler;
import top.continew.starter.core.exception.BaseException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存储处理器工厂
 * <p>按类型分发 StorageHandler</p>
 * 
 * @author KAI
 * @since 2025/07/24 13:35
 */
@Component
public class StorageHandlerFactory {
    private final Map<StorageTypeEnum, StorageHandler> HANDLER_MAP = new ConcurrentHashMap<>();

    @Autowired
    public StorageHandlerFactory(List<StorageHandler> handlers) {
        for (StorageHandler handler : handlers) {
            HANDLER_MAP.put(handler.getType(), handler);
        }
    }

    /**
     * 获取指定类型的存储处理器
     *
     * @param type 存储类型
     * @return StorageHandler
     */
    public StorageHandler createHandler(StorageTypeEnum type) {
        return Optional.ofNullable(HANDLER_MAP.get(type))
            .orElseThrow(() -> new BaseException(StrUtil.format("不存在此类型存储处理器:{}: ", type)));
    }
}