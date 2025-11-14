package top.codestyle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.codestyle.entity.LibraryMember;

/**
 * 模板人员关系Mapper
 *
 * @author huxc2020
 */
@Mapper
public interface LibraryMemberMapper extends BaseMapper<LibraryMember> {
}

