
package top.codestyle.context;

import lombok.Data;
import top.codestyle.enums.DataScopeEnum;


import java.io.Serial;
import java.io.Serializable;

/**
 * 角色上下文
 *
 * @author GALAwang
 * @since 2023/3/7 22:08
 */
@Data
public class RoleContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 角色编码
     */
    private String code;

    /**
     * 数据权限
     */
    private DataScopeEnum dataScope;
}
