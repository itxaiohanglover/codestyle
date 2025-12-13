

package top.codestyle.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.codestyle.services.RemoteMetaSearchService;
import top.codestyle.vo.SearchMetaVO;

import java.util.Optional;

/**
 * @author ChonghaoGao
 * @date 2025/12/1 19:21)
 */
@Slf4j
@RestController
@RequestMapping("/mcp")
@AllArgsConstructor
public class RemoteMetaSearchController {

    private final RemoteMetaSearchService remoteMetaSearchService;

    @GetMapping("/search")
    private ResponseEntity<?> metaSearch(@RequestParam String query) {

        Optional<SearchMetaVO> searchMetaVO = remoteMetaSearchService.search(query);
        log.info("{}", searchMetaVO);
        if (searchMetaVO.isPresent()) {
            return ResponseEntity.ok(searchMetaVO.get());
        } else {
            // 查询失败时返回带有错误信息的响应
            return ResponseEntity.status(404).body("未找到匹配关键词: \"" + query + "\" 的模板");
        }
    }
}
