
package top.codestyle.config.file;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import top.codestyle.enums.DisEnableStatusEnum;
import top.codestyle.service.StorageService;
import top.codestyle.model.dto.file.StorageReq;
import top.codestyle.model.query.StorageQuery;
import top.codestyle.model.vo.StorageResp;

import java.util.List;

/**
 * 文件存储配置加载器
 *
 * @author Charles7c
 * @since 2023/12/24 22:31
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "file.storage.config.load", havingValue = "true", matchIfMissing = false)
public class FileStorageConfigLoader implements ApplicationRunner {

    private final StorageService storageService;

    @Override
    public void run(ApplicationArguments args) {
        StorageQuery query = new StorageQuery();
        query.setStatus(DisEnableStatusEnum.ENABLE);
        List<StorageResp> storageList = storageService.list(query, null);
        if (CollUtil.isEmpty(storageList)) {
            return;
        }
        storageList.forEach(s -> storageService.load(BeanUtil.copyProperties(s, StorageReq.class)));
    }
}

