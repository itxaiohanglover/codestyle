package top.codestyle.constant;

/**
 * 系统相关常量
 *
 * @author Charles7c
 * @since 2023/2/9 22:11
 */
public class SysConstants {

    /**
     * 否
     */
    public static final Integer NO = 0;

    /**
     * 是
     */
    public static final Integer YES = 1;

    /**
     * 超管用户 ID
     */
    public static final Long SUPER_USER_ID = 1L;

    /**
     * 顶级部门 ID
     */
    public static final Long SUPER_DEPT_ID = 1L;

    /**
     * 顶级父 ID
     */
    public static final Long SUPER_PARENT_ID = 0L;

    /**
     * 超管角色编码
     */
    public static final String SUPER_ROLE_CODE = "admin";

    /**
     * 超管角色 ID
     */
    public static final Long SUPER_ROLE_ID = 1L;

    /**
     * 全部权限标识
     */
    public static final String ALL_PERMISSION = "*:*:*";

    /**
     * 账号登录 URI
     */
    public static final String LOGIN_URI = "/auth/account";

    /**
     * 退出 URI
     */
    public static final String LOGOUT_URI = "/auth/logout";

    /**
     * 描述类字段后缀
     */
    public static final String DESCRIPTION_FIELD_SUFFIX = "String";

    private SysConstants() {
    }
}
