package com.autohome.lemon.dbcheck.util;

import java.util.List;

import com.autohome.lemon.dbcheck.contract.ProjectDbConfig;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Jdbc帮助类
 *
 * @author hantianwei
 */
public class JdbcUtil {

    /**
     * 查询SQL返回List
     *
     * @param projectDbConfig 项目数据库本配置
     * @param sql             SQL
     * @param tClass          类型
     * @param <T>             泛型
     * @return List
     */
    public static <T> List<T> executeDataSql(ProjectDbConfig projectDbConfig, String sql, Class<T> tClass) {
        JdbcTemplate jdbcTemplate = new DruidUtil().getJdbcTemplate(projectDbConfig);
        List<T> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper(tClass));
        return list;
    }

}
