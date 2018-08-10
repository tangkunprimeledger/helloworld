package com.higgs.trust.consensus.p2pvalid.mybatis.extend;

import com.alibaba.fastjson.JSON;
import com.higgs.trust.consensus.p2pvalid.core.ValidCommand;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;
import java.sql.*;

/**
 * @author cwy
 */
@MappedTypes(ValidCommand.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class ValidCommandHandler implements TypeHandler<ValidCommand<?>> {
    @Override
    public void setParameter(PreparedStatement preparedStatement, int i, ValidCommand<?> validCommand, JdbcType jdbcType) throws SQLException {
        if (validCommand == null){
            preparedStatement.setNull(i, Types.VARCHAR);

        } else {
            preparedStatement.setString(i, JSON.toJSONString(validCommand));
        }
    }

    @Override
    public ValidCommand<?> getResult(ResultSet resultSet, String s) throws SQLException {
        String jsonStr = resultSet.getString(s);
        if(StringUtils.isEmpty(jsonStr)){
            return null;
        }
        return (ValidCommand<?>) JSON.parseObject(jsonStr, ValidCommand.class);
    }

    @Override
    public ValidCommand<?> getResult(ResultSet resultSet, int i) throws SQLException {
        String jsonStr = resultSet.getString(i);
        if(StringUtils.isEmpty(jsonStr)){
            return null;
        }
        return (ValidCommand<?>) JSON.parseObject(jsonStr, ValidCommand.class);
    }

    @Override
    public ValidCommand<?> getResult(CallableStatement callableStatement, int i) throws SQLException {
        String jsonStr = callableStatement.getString(i);
        if(StringUtils.isEmpty(jsonStr)){
            return null;
        }
        return (ValidCommand<?>) JSON.parseObject(jsonStr, ValidCommand.class);
    }
}
