package top.codestyle.service.impl;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.codestyle.mapper.LibraryInfoCommentMapper;
import top.codestyle.model.entity.LibraryInfoComment;
import top.codestyle.model.entity.User;
import top.codestyle.service.LibraryInfoCommentService;
import top.codestyle.service.UserService;


import java.util.Objects;

/**
 * @author LENOVO
 * @description 针对表【library_info_comment(模板库评论)】的数据库操作Service实现
 * @createDate 2025-11-11 16:32:12
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LibraryInfoCommentServiceImpl extends ServiceImpl<LibraryInfoCommentMapper, LibraryInfoComment>
        implements LibraryInfoCommentService {
    private final LibraryInfoCommentMapper mapper;
    private final UserService userService;

    /**
     * 发一级评论
     */
    public void postRoot(HttpServletRequest request, Long infoId, String content) {
        LibraryInfoComment c = new LibraryInfoComment();
        c.setInfoId(infoId);
        User loginUser = userService.getLoginUser(request);
        c.setUserId(loginUser.getId());
        c.setParentId(0L);
        c.setContent(content);
        c.setReplyCnt(0);
        c.setIsDeleted(0);
        mapper.insert(c);
    }

    /**
     * 回复评论：事务内两步
     */
    @Transactional(rollbackFor = Exception.class)
    public void postReply(HttpServletRequest request, Long infoId, Long parentId, String content) {
        LibraryInfoComment c = new LibraryInfoComment();
        c.setInfoId(infoId);
        User loginUser = userService.getLoginUser(request);
        c.setUserId(loginUser.getId());
        c.setParentId(parentId);
        c.setContent(content);
        c.setReplyCnt(0);
        c.setIsDeleted(0);
        mapper.insert(c);

        // 父级回复数+1
        mapper.update(null, Wrappers.<LibraryInfoComment>lambdaUpdate()
                .setSql("reply_cnt = reply_cnt + 1")
                .eq(LibraryInfoComment::getId, parentId));
    }

    @Override
    public int deleteOwnCommentSubtree(HttpServletRequest request, Long infoId) {
        User loginUser = userService.getLoginUser(request);
        return mapper.deleteOwnCommentSubtree(infoId, loginUser.getId());
    }

    @Override
    public int deleteCommentSubtree(HttpServletRequest request, Long infoId) {
        User loginUser = userService.getLoginUser(request);
        if(Objects.equals(loginUser.getUserRole(), "admin")){
            return mapper.deleteCommentSubtree(infoId);
        }
        return -1;
    }

}




