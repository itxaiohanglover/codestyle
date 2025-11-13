package top.codestyle.service;

import org.springframework.data.domain.Page;
import top.codestyle.entity.es.CodeStyleTemplate;

/**
 * @author ChonghaoGao
 * @date 2025/11/9 20:04
 * 用于存放一些传统的ES检索设置
 *
 */
public interface HomePageSearchService {

    Page<CodeStyleTemplate> searchHomePage(String keyword, int page, int size);

}
