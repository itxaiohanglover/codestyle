package top.codestyle.pojo.dto;

import lombok.Data;
import org.springframework.data.domain.Page;
import java.util.List;
/**
 * @author ChonghaoGao
 * @date 2025/11/19 15:30)
 */
@Data
public class PageResponse<T> {
    private int page;          // 1-based 页码
    private int size;          
    private long totalElements;
    private int totalPages;
    private List<T> content;
    private boolean first;
    private boolean last;
    
    public PageResponse(Page<T> pageData) {
        this.page = pageData.getNumber() + 1;  // 转换为 1-based
        this.size = pageData.getSize();
        this.totalElements = pageData.getTotalElements();
        this.totalPages = pageData.getTotalPages();
        this.content = pageData.getContent();
        this.first = pageData.isFirst();
        this.last = pageData.isLast();
    }

    // getter/setter ...
}
