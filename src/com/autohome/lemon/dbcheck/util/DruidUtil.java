package com.autohome.lemon.dbcheck.util;

import java.sql.SQLException;
import java.util.HashMap;
import javax.sql.DataSource;

import com.alibaba.druid.pool.DruidDataSource;
import com.autohome.lemon.dbcheck.constant.AutoConstant;
import com.autohome.lemon.dbcheck.contract.DatabaseConfig;
import com.autohome.lemon.dbcheck.contract.ProjectDbConfig;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Druid Util
 *
 * @author hantianwei
 */
public class DruidUtil {

    /**
     * 本地缓存
     */
    public static HashMap<String, DruidDataSource> dataSourceMap = new HashMap<String, DruidDataSource>();

    /**
     * 根据项目数据库配置创建DruidDataSource
     *
     * @param projectDbConfig 项目数据库配置
     * @return DruidDataSource
     */
    private DruidDataSource createDataSource(ProjectDbConfig projectDbConfig) {
        DatabaseConfig databaseConfig = ConfigUtil.getDatabaseConfigDefault(projectDbConfig.getDatabaseType());
        DruidDataSource datasource = new DruidDataSource();
        datasource.setUrl(String.format(databaseConfig.getJdbcUrl(), projectDbConfig.getUrl(), projectDbConfig.getDatabaseName()));
        datasource.setUsername(projectDbConfig.getUserName());
        datasource.setPassword(projectDbConfig.getPassword());
        datasource.setDriverClassName(databaseConfig.getJdbcDriver());

        //configuration
        datasource.setInitialSize(2);
        datasource.setMinIdle(2);
        datasource.setMaxActive(200);
        datasource.setMaxWait(10000);
        datasource.setTimeBetweenEvictionRunsMillis(60000);
        datasource.setMinEvictableIdleTimeMillis(300000);
        datasource.setValidationQuery("SELECT 'x'");
        datasource.setTestWhileIdle(false);
        datasource.setTestOnBorrow(false);
        datasource.setTestOnReturn(false);
        datasource.setPoolPreparedStatements(true);
        datasource.setMaxPoolPreparedStatementPerConnectionSize(20);
        datasource.setConnectionErrorRetryAttempts(1);
        datasource.setNotFullTimeoutRetryCount(1);
        datasource.setBreakAfterAcquireFailure(true);
        try {
            datasource.setFilters("config");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        datasource.setConnectionProperties("config.decrypt=false;");
        return datasource;
    }

    /**
     * 根据项目数据库配置取DataSource
     *
     * @param projectDbConfig 项目数据库配置
     * @return DataSource
     */
    public DataSource getDataSource(ProjectDbConfig projectDbConfig) {

        String dataSourceKey = String.format("dataSource-%s-%s", projectDbConfig.getUrl(), projectDbConfig.getDatabaseName());
        DruidDataSource dataSource = dataSourceMap.get(dataSourceKey);
        if (dataSource == null
                || dataSource.isClosed()
                || !dataSource.isEnable()) {
            dataSource = createDruidDataSourceAndMap(projectDbConfig, dataSourceKey);
        }
        return dataSource;
    }

    /**
     * 创建DruidDataSource并写入本地缓存
     *
     * @param projectDbConfig 项目数据库配置
     * @param dataSourceKey   缓存KEY
     * @return DruidDataSource
     */
    private DruidDataSource createDruidDataSourceAndMap(ProjectDbConfig projectDbConfig,
                                                        String dataSourceKey) {

        DruidDataSource dataSource = dataSourceMap.get(dataSourceKey);
        if (dataSource != null) {
            dataSourceMap.remove(dataSourceKey);
        }
        dataSource = createDataSource(projectDbConfig);
        dataSourceMap.put(dataSourceKey, dataSource);
        return dataSource;
    }

    /**
     * 取JdbcTemplate,如果缓存存在则直接取缓存中的返回
     *
     * @return JdbcTemplate
     */
    public JdbcTemplate getJdbcTemplate(ProjectDbConfig projectDbConfig) {
        if (projectDbConfig == null) {
            throw new NullPointerException(AutoConstant.CONFIGURE_ITEM_INCOMPLETE);
        }
        DataSource dataSource = getDataSource(projectDbConfig);
        JdbcTemplate template = new JdbcTemplate(dataSource);
        return template;
    }

    /**
     * 清理DataSource
     */
    public static void clearDataSource() {
        DruidUtil.dataSourceMap.forEach((k, v) -> {
            if (!v.isClosed()) {
                v.close();
            }
        });
        DruidUtil.dataSourceMap = new HashMap<String, DruidDataSource>();
    }

}
