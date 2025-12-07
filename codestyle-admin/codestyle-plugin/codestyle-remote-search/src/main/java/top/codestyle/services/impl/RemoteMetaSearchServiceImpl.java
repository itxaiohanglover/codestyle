package top.codestyle.services.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.codestyle.respository.es.RemoteSearchESRepository;
import top.codestyle.services.RemoteMetaSearchService;
import top.codestyle.vo.SearchMetaVO;

import java.util.Optional;

/**
 * @author ChonghaoGao
 * @date 2025/12/1 19:06)
 */
@Slf4j
@Service
@AllArgsConstructor
public class RemoteMetaSearchServiceImpl implements RemoteMetaSearchService {

    private final RemoteSearchESRepository repository;

    @Override
    public Optional<SearchMetaVO> search(String query) {
        try{
            return repository.searchInES(query);
        }catch (Exception e){
            log.info("检索异常:{},尝试返回兜底数据",e.getMessage());
        }
        return Optional.empty();
    }
}
