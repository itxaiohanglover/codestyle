

package top.codestyle.services;

import top.codestyle.vo.SearchMetaVO;

import java.util.Optional;

/**
 * @author ChonghaoGao
 * @date 2025/12/1 19:06)
 */
public interface RemoteMetaSearchService {
    Optional<SearchMetaVO> search(String query);
}
