package top.codestyle.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.codestyle.common.BaseResponse;
import top.codestyle.common.ResultUtils;
import top.codestyle.service.LibraryInfoCommentService;




@RestController
@RequestMapping("/Comment")
public class LibraryInfoCommentController {
    @Resource
    private LibraryInfoCommentService libraryInfoCommentService;

    /**
     * 0级评论
     */
    @PostMapping("/root")
    public void postRoot(HttpServletRequest request, Long infoId, String content) {
        libraryInfoCommentService.postRoot(request,infoId,content);
    }

    /**
     * 子级评论
     */
    @PostMapping("/replay")
    public void postReply(HttpServletRequest request, Long infoId, Long parentId, String content) {
        libraryInfoCommentService.postReply(request,infoId,parentId,content);
    }

    /**
     * 删除评论
     */
    @GetMapping("/deleteOwn")
    public BaseResponse<?> deleteOwnComment(HttpServletRequest request, Long infoId) {
        int i = libraryInfoCommentService.deleteOwnCommentSubtree(request, infoId);
        return ResultUtils.success("已删除"+i+"条评论");
    }


    @GetMapping("/delete")
    public BaseResponse<?> deleteComment(HttpServletRequest request, Long infoId) {
        int i = libraryInfoCommentService.deleteCommentSubtree(request, infoId);
        if (i == -1){
            return ResultUtils.error(500,"用户权限不足");
        }
            return ResultUtils.success("已删除"+i+"条评论");
    }

}
