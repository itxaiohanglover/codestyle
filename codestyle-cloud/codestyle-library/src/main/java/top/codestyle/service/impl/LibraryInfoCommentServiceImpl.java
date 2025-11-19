package top.codestyle.service.impl;



import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.codestyle.mapper.LibraryInfoCommentMapper;
import top.codestyle.model.entity.LibraryInfoComment;
import top.codestyle.service.LibraryInfoCommentService;

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

}




