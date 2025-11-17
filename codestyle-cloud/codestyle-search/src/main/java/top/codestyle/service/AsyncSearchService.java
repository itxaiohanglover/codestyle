package top.codestyle.service;

import org.springframework.data.domain.Page;
import top.codestyle.entity.es.pojo.CodeStyleTemplateDO;
import top.codestyle.entity.es.vo.HomePageSearchResultVO;

import java.util.concurrent.CompletableFuture;

/**
 * @author ChonghaoGao
 * @date 2025/11/12 21:59
 * 实验级原型接口 用于解决单端并发问题 实现异步化搜索能力增强单节点搜索的能力
 */
public interface AsyncSearchService {
    public CompletableFuture<Page<HomePageSearchResultVO>> searchAsync(String keyword, int page, int size);
}
