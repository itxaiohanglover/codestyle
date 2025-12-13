package top.codestyle.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Canal配置属性类
 * 封装Canal的所有配置项
 *
 * @author ChonghaoGao
 * @date 2025/12/13
 */
@Data
@Component
@ConfigurationProperties(prefix = "canal.client.instances.example")
public class CanalProperties {
    
    /**
     * Canal服务器主机地址
     */
    private String host = "localhost";
    
    /**
     * Canal服务器端口
     */
    private int port = 11111;
    
    /**
     * Canal实例名称
     */
    private String instance = "example";
    
    /**
     * 数据库用户名
     */
    private String username = "root";
    
    /**
     * 数据库密码
     */
    private String password = "root";
    
    /**
     * 批量获取消息的大小
     */
    private int batchSize = 1000;
    
    /**
     * 轮询超时时间，单位毫秒
     */
    private long timeout = 3000;
    
    /**
     * 数据库配置
     */
    private Db db = new Db();
    
    /**
     * 数据库配置内部类
     */
    @Data
    public static class Db {
        /**
         * 数据库用户名
         */
        private String username = "root";
        
        /**
         * 数据库密码
         */
        private String password = "root";
    }
}
