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

package top.codestyle.admin.search.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.codestyle.admin.search.config.SearchProperties;
import top.codestyle.admin.search.service.EmbeddingService;

/**
 * Embedding 服务实现
 * <p>
 * TODO: 集成实际的 Embedding 模型（如 BGE-M3）
 * 当前使用简单的哈希算法生成模拟向量
 *
 * @author CodeStyle Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingServiceImpl implements EmbeddingService {

    private final SearchProperties searchProperties;

    @Override
    public float[] encode(String text) {
        log.debug("将文本转换为向量: {}", text.substring(0, Math.min(50, text.length())));

        int dimension = searchProperties.getMilvus().getDimension();
        float[] vector = new float[dimension];

        // TODO: 调用实际的 Embedding API
        // 当前使用简单的哈希算法生成模拟向量
        int hash = text.hashCode();
        for (int i = 0; i < dimension; i++) {
            vector[i] = (float) Math.sin(hash + i);
        }

        // 归一化向量
        normalizeVector(vector);

        return vector;
    }

    @Override
    public float[][] encodeBatch(String[] texts) {
        log.debug("批量将 {} 个文本转换为向量", texts.length);

        float[][] vectors = new float[texts.length][];
        for (int i = 0; i < texts.length; i++) {
            vectors[i] = encode(texts[i]);
        }

        return vectors;
    }

    /**
     * 归一化向量（L2 范数）
     */
    private void normalizeVector(float[] vector) {
        double sum = 0.0;
        for (float v : vector) {
            sum += v * v;
        }
        double norm = Math.sqrt(sum);

        if (norm > 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] /= norm;
            }
        }
    }
}

