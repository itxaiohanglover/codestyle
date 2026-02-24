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

package top.codestyle.admin.system.factory;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.utils.SdkAutoCloseable;
import top.codestyle.admin.system.model.entity.StorageDO;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 异步 S3 客户端工厂
 * <p>支持多 endpoint / 多 accessKey 的动态客户端池</p>
 */
@Slf4j
@Component
public class S3ClientFactory {

    private final ConcurrentHashMap<String, S3Client> CLIENT_CACHE = new ConcurrentHashMap<>();

    public S3Client getClient(StorageDO storage) {
        String key = storage.getEndpoint() + "|" + storage.getAccessKey();
        return CLIENT_CACHE.computeIfAbsent(key, k -> {
            StaticCredentialsProvider auth = StaticCredentialsProvider.create(AwsBasicCredentials.create(storage
                .getAccessKey(), storage.getSecretKey()));
            return S3Client.builder()
                .credentialsProvider(auth)
                .endpointOverride(URI.create(storage.getEndpoint()))
                .region(Region.US_EAST_1)
                .serviceConfiguration(S3Configuration.builder().chunkedEncodingEnabled(false).build())
                .build();
        });
    }

    @PreDestroy
    public void closeAll() {
        CLIENT_CACHE.values().forEach(SdkAutoCloseable::close);
    }
}
