package top.codestyle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import top.codestyle.entity.VipPo;

/**
 * VIP用户Mapper
 *
 * @author huxc2020
 */
@Mapper
public interface UserMapper extends BaseMapper<VipPo> {

    /**
     * 根据用户ID查询用户等级
     * 查询vip_user表，如果能找到记录且expired_time大于当前时间，则返回1（VIP），否则返回0（普通用户）
     *
     * @param userId 用户ID
     * @return 用户等级（0-普通用户，1-VIP用户）
     */
    @Select("SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END " +
            "FROM vip_user " +
            "WHERE vip_user = #{userId} AND expired_time > NOW()")
    Integer getUserLevelByUserId(Long userId);
}

