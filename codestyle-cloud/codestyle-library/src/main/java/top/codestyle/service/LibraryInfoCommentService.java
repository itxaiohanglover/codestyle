package top.codestyle.service;


import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import top.codestyle.model.entity.LibraryInfoComment;

/**
 * @author LENOVO
 * @description 针对表【library_info_comment(模板库评论)】的数据库操作Service
 * @createDate 2025-11-11 16:32:12
 */
public interface LibraryInfoCommentService extends IService<LibraryInfoComment> {
    void postRoot(HttpServletRequest request, Long infoId, String content);

    void postReply(HttpServletRequest request, Long infoId, Long parentId, String content);

    int deleteOwnCommentSubtree(HttpServletRequest request, Long infoId);

    int deleteCommentSubtree(HttpServletRequest request, Long infoId);
}
