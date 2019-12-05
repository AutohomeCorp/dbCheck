package com.autohome.lemon.dbcheck.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.autohome.lemon.dbcheck.contract.CheckGuideExistsColumn;
import com.autohome.lemon.dbcheck.contract.DatabaseConfig;
import com.autohome.lemon.dbcheck.contract.ProjectConfig;
import com.autohome.lemon.dbcheck.contract.ProjectDbConfig;
import com.autohome.lemon.dbcheck.contract.TableColumn;
import com.autohome.lemon.dbcheck.contract.TreeNode;
import com.autohome.lemon.dbcheck.util.ConfigUtil;
import com.autohome.lemon.dbcheck.util.JdbcUtil;
import com.autohome.lemon.dbcheck.util.NotificationUtil;
import com.intellij.openapi.project.Project;
import org.apache.commons.collections.CollectionUtils;

/**
 * 检测字段相关
 *
 * @author hantianwei
 */
public class CheckColumn {

    /**
     * Project
     */
    private Project project;

    /**
     * 项目配置
     */
    private ProjectConfig projectConfig;

    /**
     * 库里字段列表
     */
    private List<TableColumn> tableColumnList = new ArrayList<>();

    /**
     * 构造函数
     *
     * @param project       Project
     * @param projectConfig 项目配置
     */
    public CheckColumn(Project project, ProjectConfig projectConfig) {
        this.project = project;
        this.projectConfig = projectConfig;
        this.getAllTableColumns();
    }

    /**
     * build
     *
     * @param project       Project
     * @param projectConfig 项目配置
     * @return CheckColumn
     */
    public static CheckColumn build(Project project, ProjectConfig projectConfig) {
        return new CheckColumn(project, projectConfig);
    }

    /**
     * 执行检测
     *
     * @param columnNodeGuideNotExists 未加字段列表
     */
    public void execute(List<TreeNode> columnNodeGuideNotExists) {
        try {
            checkGuide(columnNodeGuideNotExists);
        } catch (Exception ex) {
            NotificationUtil.showTip(ex.getMessage(), this.project);
        }
    }

    /**
     * 检测规范
     *
     * @param columnNodeGuideNotExists 未加字段列表
     */
    private void checkGuide(List<TreeNode> columnNodeGuideNotExists) {
        for (ProjectDbConfig dbConfig : projectConfig.getDbConfigs()) {
            List<TableColumn> dbColumnList = tableColumnList
                    .stream()
                    .filter(t -> t.getDbName().toLowerCase().equals(dbConfig.getDatabaseName().toLowerCase()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(dbColumnList)) {
                return;
            }
            DatabaseConfig databaseConfig = ConfigUtil.getDatabaseConfig(project.getBasePath(), dbConfig.getDatabaseType());
            Map<String, List<TableColumn>> tableGroupList = dbColumnList.stream().collect(Collectors.groupingBy(TableColumn::getTableName));
            List<TreeNode> notExistsColumnTableList = new ArrayList<>();
            tableGroupList.forEach((k, v) -> {
                List<TreeNode> notExistsColumnList = new ArrayList<>();
                isExistsColumn(v, notExistsColumnList, databaseConfig);
                if (CollectionUtils.isNotEmpty(notExistsColumnList)) {
                    notExistsColumnTableList.add(new TreeNode(k, notExistsColumnList));
                }
            });
            if (CollectionUtils.isNotEmpty(notExistsColumnTableList)) {
                columnNodeGuideNotExists.add(new TreeNode(String.format("%s[%s]",
                        dbConfig.getDatabaseName(),
                        databaseConfig.getDatabaseType().toUpperCase()), notExistsColumnTableList));
            }
        }
    }

    /**
     * 字段是否存在
     *
     * @param columnList          字段列表
     * @param notExistsColumnList 未加字段列表
     * @param databaseConfig      数据配置
     */
    private void isExistsColumn(List<TableColumn> columnList, List<TreeNode> notExistsColumnList, DatabaseConfig databaseConfig) {
        List<CheckGuideExistsColumn> checkGuideExistsColumnList = databaseConfig.getExistsColumn();
        List<String> ingoreColumn = databaseConfig.getIngoreColumn();
        checkGuideExistsColumnList.forEach(item -> {
            if (columnList.stream().filter(t -> item.getColumnName().equals(t.getColumnName().toLowerCase())).findFirst().orElse(null) == null) {
                if (!ingoreColumn.contains(item.getColumnName().toLowerCase())) {
                    notExistsColumnList.add(new TreeNode(item.getColumnName(), null));
                }
            }
        });

    }

    /**
     * 返回当前库所有表和字段信息
     *
     * @return 所有表和字段信息
     */
    public void getAllTableColumns() {
        if (projectConfig == null || CollectionUtils.isEmpty(projectConfig.getDbConfigs())) {
            return;
        }
        for (ProjectDbConfig projectDbConfig : projectConfig.getDbConfigs()) {
            DatabaseConfig databaseConfig = ConfigUtil.getDatabaseConfig(project.getBasePath(), projectDbConfig.getDatabaseType());
            String sql = String.format(databaseConfig.getSelectColumnSql(), projectDbConfig.getDatabaseName());
            List<TableColumn> columnList = JdbcUtil.executeDataSql(projectDbConfig, sql, TableColumn.class);
            tableColumnList.addAll(columnList);
        }
    }
}
