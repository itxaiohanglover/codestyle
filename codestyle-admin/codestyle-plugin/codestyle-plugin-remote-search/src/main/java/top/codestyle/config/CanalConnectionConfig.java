package top.codestyle.config;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.codestyle.properties.CanalProperties;

import java.net.InetSocketAddress;

/**
 * Canal连接配置类
 * 专门负责Canal连接池的创建和管理
 * 与消息监听逻辑分离，降低耦合度
 *
 * @author ChonghaoGao
 * @date 2025/12/13
 */
@Slf4j
@Configuration
public class CanalConnectionConfig {

    @Resource
    private CanalProperties canalProperties;

    /**
     * 创建Canal连接器
     *
     * @return CanalConnector实例
     */
    @Bean(destroyMethod = "disconnect")
    public CanalConnector canalConnector() {
        log.info("初始化Canal连接器，地址: {}:{}, 实例: {}", 
                canalProperties.getHost(), 
                canalProperties.getPort(), 
                canalProperties.getInstance());
        
        // 创建连接器
        CanalConnector connector = CanalConnectors.newSingleConnector(
                new InetSocketAddress(canalProperties.getHost(), canalProperties.getPort()),
                canalProperties.getInstance(),
                canalProperties.getDb().getUsername(),
                canalProperties.getDb().getPassword()
        );
        
        // 连接
        connector.connect();
        log.info("Canal连接器连接成功");
        
        // 订阅所有表
        connector.subscribe("meta_info_db.tb_remote_meta_info");
        log.info("Canal连接器订阅表成功: meta_info_db.tb_remote_meta_info");
        
        // 回滚到上一条确认的位置
        connector.rollback();
        log.info("Canal连接器初始化完成");
        
        return connector;
    }
}
