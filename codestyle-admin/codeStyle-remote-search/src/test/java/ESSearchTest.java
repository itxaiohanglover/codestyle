import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import top.codestyle.respository.es.RemoteSearchESRepository;
import top.codestyle.vo.SearchMetaVO;

import java.util.Optional;

/**
 * ES检索测试类
 * 用于测试ES客户端检索功能是否正常工作
 *
 * @author ChonghaoGao
 * @date 2025/12/4
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = top.codestyle.RemoteSearchApplication.class)
public class ESSearchTest {

    @Autowired
    private RemoteSearchESRepository remoteSearchESRepository;
    
    /**
     * 测试ES检索功能
     * 搜索"test-updated"，验证是否能检索到之前更新的数据
     */
    @Test
    public void testESSearch() {
        System.out.println("开始测试ES检索功能...");
        
        // 搜索"test-updated"
        Optional<SearchMetaVO> searchResult = remoteSearchESRepository.searchInES("test-updated");
        
        System.out.println("搜索结果: " + searchResult);
        
        // 验证是否检索到了结果
        if (searchResult.isPresent()) {
            System.out.println("成功检索到数据！");
            System.out.println("检索到的结果: " + searchResult.get());
        } else {
            System.out.println("未检索到数据！");
        }
    }
}
