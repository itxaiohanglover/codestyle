
package top.codestyle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.codestyle.model.mysql.entity.RemoteMetaInfo;
import java.util.Date;
import java.util.List;

/**
 * @author ChonghaoGao
 * @date 2025/12/1 19:51
 */
@Mapper // 添加@Mapper注解，确保MyBatis能识别这个Mapper
// 使用专门为Spring Boot 3.x设计的mybatis-plus-spring-boot3-starter
public interface RemoteMetaInfoMapper extends BaseMapper<RemoteMetaInfo> {
    // 继承BaseMapper，获得所有CRUD方法

    // 自定义查询方法：获取所有已删除的数据，绕过MyBatis-Plus的逻辑删除自动过滤
    List<RemoteMetaInfo> selectAllDeletedData();

    // 自定义查询方法：获取所有已恢复的数据（deleted=0）
    List<RemoteMetaInfo> selectAllRestoredData();

    // 自定义查询方法：获取指定时间后更新的数据，包括已删除的
    List<RemoteMetaInfo> selectUpdatedDataAfterTime(Date lastSyncTime);
    
    // 自定义查询方法：根据ID获取数据，包括已删除的
    RemoteMetaInfo selectByIdIncludeDeleted(Long id);
}
