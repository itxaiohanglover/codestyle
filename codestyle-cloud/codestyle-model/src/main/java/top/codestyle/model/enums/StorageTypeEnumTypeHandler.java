// java
package top.codestyle.model.enums;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import top.codestyle.model.enums.StorageTypeEnum;

import java.sql.*;

/**
 * MyBatis 类型处理器：在 ResultSet/PreparedStatement 之间转换 StorageTypeEnum <-> int
 */
@MappedTypes(StorageTypeEnum.class)
public class StorageTypeEnumTypeHandler extends BaseTypeHandler<StorageTypeEnum> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, StorageTypeEnum parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getValue());
    }

    @Override
    public StorageTypeEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int code = rs.getInt(columnName);
        if (rs.wasNull()) return null;
        return StorageTypeEnum.fromValue(code);
    }

    @Override
    public StorageTypeEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int code = rs.getInt(columnIndex);
        if (rs.wasNull()) return null;
        return StorageTypeEnum.fromValue(code);
    }

    @Override
    public StorageTypeEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int code = cs.getInt(columnIndex);
        if (cs.wasNull()) return null;
        return StorageTypeEnum.fromValue(code);
    }
}