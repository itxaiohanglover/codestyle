package top.codestyle.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import top.codestyle.model.entity.LibraryInfoComment;

/**
* @author LENOVO
* @description 针对表【library_info_comment(模板库评论)】的数据库操作Mapper
* @createDate 2025-11-11 16:32:12
* @Entity .LibraryInfoComment
*/
public interface LibraryInfoCommentMapper extends BaseMapper<LibraryInfoComment> {

        @Update({"WITH RECURSIVE cte AS ( " +
                "  SELECT id FROM library_info_comment " +
                "  WHERE id = #{rootId} AND user_id = #{userId} AND is_deleted = 0 " +
                "  UNION ALL " +
                "  SELECT c.id FROM library_info_comment c JOIN cte ON c.parent_id = cte.id WHERE c.is_deleted = 0) " +
                "UPDATE library_info_comment t " +
                "JOIN cte ON t.id = cte.id " +
                "SET t.is_deleted = 1, t.updateTime = NOW() " +
                "WHERE t.is_deleted = 0"})
        int deleteOwnCommentSubtree(@Param("rootId") Long rootId,
                                    @Param("userId") Long userId);

        @Update({"WITH RECURSIVE cte AS ( " +
                "  SELECT id FROM library_info_comment WHERE id = #{rootId} " +
                "  UNION ALL " +
                "  SELECT c.id FROM library_info_comment c JOIN cte ON c.parent_id = cte.id WHERE c.is_deleted = 0) " +
                "UPDATE library_info_comment t " +
                "JOIN cte ON t.id = cte.id " +
                "SET t.is_deleted = 1, t.updateTime = NOW() " +
                "WHERE t.is_deleted = 0"})
        int deleteCommentSubtree(@Param("rootId") Long rootId);

}




