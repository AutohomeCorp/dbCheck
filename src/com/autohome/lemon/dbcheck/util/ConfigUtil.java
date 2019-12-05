package com.autohome.lemon.dbcheck.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.autohome.lemon.dbcheck.config.PersistentState;
import com.autohome.lemon.dbcheck.constant.AutoConstant;
import com.autohome.lemon.dbcheck.constant.DbTypeEnum;
import com.autohome.lemon.dbcheck.contract.DatabaseConfig;
import com.autohome.lemon.dbcheck.contract.DatabaseConfigCustom;
import com.autohome.lemon.dbcheck.contract.KeyValuePair;
import com.autohome.lemon.dbcheck.contract.ProjectConfig;
import com.autohome.lemon.dbcheck.contract.ProjectDbConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 配置帮助类
 *
 * @author hantianwei
 */
public class ConfigUtil {

    private static PersistentState persistentState = PersistentState.getInstance();
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static Map<String, DatabaseConfig> databaseConfMap = new HashMap<>();
    private static KeyValuePair<Long, DatabaseConfigCustom> databaseConfigCustomMap = new KeyValuePair(0L, null);

    /**
     * 根据项目名称取配置信息
     *
     * @param projectName 项目名称
     * @return 配置信息
     */
    public static ProjectConfig getConfig(String projectName) {
        String config = persistentState.getConfig();
        List<ProjectConfig> projectConfigList = gson.fromJson(config, new TypeToken<List<ProjectConfig>>() {
        }.getType());
        if (CollectionUtils.isEmpty(projectConfigList)) {
            return null;
        }
        return projectConfigList
                .stream()
                .filter(t -> t.getProjectName().equals(projectName))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据项目名称和数据库名称取数据库配置信息
     *
     * @param projectName  项目名称
     * @param databaseName 数据库名称
     * @return 数据库配置信息
     */
    public static ProjectDbConfig getDbConfig(String projectName, String databaseName) {
        ProjectConfig projectConfig = getConfig(projectName);
        if (projectConfig == null
                || projectConfig.getDbConfigs() == null
                || projectConfig.getDbConfigs().size() == 0) {
            return null;
        }
        return projectConfig.getDbConfigs()
                .stream()
                .filter(t -> t.getDatabaseName().equals(databaseName))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据项目配置和Mapper路径取数据库配置
     *
     * @param projectConfig 项目配置
     * @param filePath      Mapper路径
     * @return 数据库配置
     */
    public static ProjectDbConfig getDbConfigByFilePath(ProjectConfig projectConfig,
                                                        String filePath) {
        if (projectConfig == null || StringUtils.isBlank(filePath)) {
            return null;
        }
        return projectConfig.getDbConfigs()
                .stream()
                .filter(t -> filePath.contains(t.getMapperPackagePath()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 验证数据库配置项是否配全
     *
     * @param projectDbConfig 数据库配置
     */
    public static void verifyDbConfig(ProjectDbConfig projectDbConfig) {
        if (projectDbConfig == null
                || StringUtils.isEmpty(projectDbConfig.getMapperPackagePath())
                || StringUtils.isEmpty(projectDbConfig.getDatabaseName())
                || StringUtils.isEmpty(projectDbConfig.getPassword())
                || StringUtils.isEmpty(projectDbConfig.getUrl())
                || StringUtils.isEmpty(projectDbConfig.getUserName())) {
            throw new RuntimeException(AutoConstant.CONFIGURE_ITEM_INCOMPLETE);
        }
    }

    /**
     * 验证项目配置项是否配全
     *
     * @param projectConfig 项目配置项
     */
    public static void verifyProjectConfig(ProjectConfig projectConfig) {
        if (projectConfig == null
                || StringUtils.isEmpty(projectConfig.getProjectName())
                || CollectionUtils.isEmpty(projectConfig.getDbConfigs())) {
            throw new RuntimeException(AutoConstant.CONFIGURE_ITEM_INCOMPLETE);
        }
    }

    /**
     * 根据数据库类型取默认配置
     *
     * @param databaseType 数据库类型
     * @return 配置
     */
    public static DatabaseConfig getDatabaseConfigDefault(String databaseType) {
        DatabaseConfig databaseConfig = databaseConfMap.get(databaseType.toLowerCase());
        if (databaseConfig == null) {
            databaseConfig = YmlUtil.buildLocal(String.format(AutoConstant.DATABASE_CONF_PATH, databaseType), DatabaseConfig.class);
            databaseConfMap.put(databaseType, databaseConfig);
        }
        return databaseConfig;
    }

    /**
     * 根据项目路径取自定义配置
     *
     * @param projectBasePath 项目路径
     * @return 配置
     */
    public static DatabaseConfigCustom getDatabaseConfigCustom(String projectBasePath) {
        String path = String.format("%s/%s", projectBasePath, AutoConstant.CUSTOM_CONFIG_PATH);
        long fileLastModified = FileUtil.getFileLastModified(path);
        if (fileLastModified < 0) {
            return null;
        }
        if (databaseConfigCustomMap.getKey() != fileLastModified) {
            DatabaseConfigCustom databaseConfigCustom = YmlUtil.build(path, DatabaseConfigCustom.class);
            databaseConfigCustomMap = new KeyValuePair<>(fileLastModified, databaseConfigCustom);
        }
        return databaseConfigCustomMap.getValue();
    }

    /**
     * 根据项目路径和数据类型取配置
     *
     * @param projectBasePath 项目路径
     * @param databaseType    数据库类型
     * @return 配置
     */
    public static DatabaseConfig getDatabaseConfig(String projectBasePath, String databaseType) {
        DatabaseConfig databaseConfig = getDatabaseConfigDefault(databaseType.toLowerCase());
        DatabaseConfigCustom databaseConfigCustom = getDatabaseConfigCustom(projectBasePath);
        if (databaseConfigCustom != null) {
            resetCustomConfig(databaseConfig, databaseConfigCustom);
        }
        return databaseConfig;
    }

    /**
     * 重置配置，如果有自定义节点用自定义配置
     *
     * @param databaseConfig       默认配置
     * @param databaseConfigCustom 自定义配置
     */
    private static void resetCustomConfig(DatabaseConfig databaseConfig, DatabaseConfigCustom databaseConfigCustom) {
        if (DbTypeEnum.SQL_SERVER.getValue().equals(databaseConfig.getDatabaseType())) {
            resetCustomConfig(databaseConfig, databaseConfigCustom.getSqlServer());
        } else {
            resetCustomConfig(databaseConfig, databaseConfigCustom.getMysql());
        }
    }

    /**
     * 重置配置如果有自定义节点用自定义配置
     *
     * @param databaseConfig       默认配置
     * @param databaseConfigCustom 自定义配置
     */
    private static void resetCustomConfig(DatabaseConfig databaseConfig, DatabaseConfig databaseConfigCustom) {
        if (CollectionUtils.isNotEmpty(databaseConfigCustom.getExistsColumn())) {
            databaseConfig.setExistsColumn(databaseConfigCustom.getExistsColumn());
        }
        if (CollectionUtils.isNotEmpty(databaseConfigCustom.getIngoreColumn())) {
            databaseConfig.setIngoreColumn(databaseConfigCustom.getIngoreColumn());
        }
        if (CollectionUtils.isNotEmpty(databaseConfigCustom.getIngoreIndex())) {
            databaseConfig.setIngoreIndex(databaseConfigCustom.getIngoreIndex());
        }
        if (databaseConfigCustom.getComplexSqlJoinQuantity() > 0) {
            databaseConfig.setComplexSqlJoinQuantity(databaseConfigCustom.getComplexSqlJoinQuantity());
        }
        if (StringUtils.isNotEmpty(databaseConfigCustom.getPrimaryKeyStartWith())) {
            databaseConfig.setPrimaryKeyStartWith(databaseConfigCustom.getPrimaryKeyStartWith());
        }
        if (StringUtils.isNotEmpty(databaseConfigCustom.getNormalIndexNameStartWith())) {
            databaseConfig.setNormalIndexNameStartWith(databaseConfigCustom.getNormalIndexNameStartWith());
        }
        if (StringUtils.isNotEmpty(databaseConfigCustom.getUniqueIndexNameStartWith())) {
            databaseConfig.setUniqueIndexNameStartWith(databaseConfigCustom.getUniqueIndexNameStartWith());
        }
        if (CollectionUtils.isNotEmpty(databaseConfigCustom.getRequiredWhere())) {
            databaseConfig.setRequiredWhere(databaseConfigCustom.getRequiredWhere());
        }
    }
}
