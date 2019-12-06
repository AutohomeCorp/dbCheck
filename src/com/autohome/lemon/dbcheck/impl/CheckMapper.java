package com.autohome.lemon.dbcheck.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.dialect.sqlserver.visitor.SQLServerSchemaStatVisitor;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.autohome.lemon.dbcheck.constant.AutoConstant;
import com.autohome.lemon.dbcheck.constant.CheckGuideTypeEnum;
import com.autohome.lemon.dbcheck.constant.DbTypeEnum;
import com.autohome.lemon.dbcheck.contract.DatabaseConfig;
import com.autohome.lemon.dbcheck.contract.ProjectConfig;
import com.autohome.lemon.dbcheck.contract.ProjectDbConfig;
import com.autohome.lemon.dbcheck.contract.TableIndex;
import com.autohome.lemon.dbcheck.contract.TreeNode;
import com.autohome.lemon.dbcheck.util.ConfigUtil;
import com.autohome.lemon.dbcheck.util.JdbcUtil;
import com.autohome.lemon.dbcheck.util.NotificationUtil;
import com.autohome.lemon.dbcheck.util.RegexUtil;
import com.autohome.lemon.dbcheck.util.StringUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.xml.XmlFileImpl;
import com.intellij.psi.xml.XmlTag;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.session.Configuration;

/**
 * 检测 Mybatis Maper
 *
 * @author hantianwei
 */
public class CheckMapper {

    /**
     * Project
     */
    private Project project;

    /**
     * 项目配置
     */
    private ProjectConfig projectConfig;

    /**
     * 数据库已加索引列表
     */
    private List<TableIndex> tableIndexList;

    /**
     * 构造函数
     *
     * @param project       Project
     * @param projectConfig 项目配置
     */
    public CheckMapper(Project project, ProjectConfig projectConfig) {
        this.project = project;
        this.projectConfig = projectConfig;
        this.tableIndexList = this.getAllIndex(projectConfig);
    }

    /**
     * build
     *
     * @param project       Project
     * @param projectConfig 项目配置
     * @return CheckMapper
     */
    public static CheckMapper build(Project project, ProjectConfig projectConfig) {
        return new CheckMapper(project, projectConfig);
    }

    /**
     * 执行检测
     *
     * @param fileList                    需要检测的Mapper文件列表
     * @param indexGuideNotExistsNodeList 未加索引列表
     * @param indexGuideNormalNodeList    普通索引命名不规范列表
     * @param indexGuideUniqNodeList      唯一索引命名不规范列表
     * @param complexSqlNodeList          复杂SQL列表
     * @param whereExistsCheckList        WHERE条件确实必加列表
     * @return
     */
    public void execute(List<VirtualFile> fileList,
                        List<TreeNode> indexGuideNotExistsNodeList,
                        List<TreeNode> indexGuideNormalNodeList,
                        List<TreeNode> indexGuideUniqNodeList,
                        List<TreeNode> complexSqlNodeList,
                        List<TreeNode> whereExistsCheckList) {
        try {
            checkGuide(indexGuideNormalNodeList, CheckGuideTypeEnum.INDEX_NORMAL_NAME);
            checkGuide(indexGuideUniqNodeList, CheckGuideTypeEnum.INDEX_UNIQ_NAME);
            checkDbs(fileList, indexGuideNotExistsNodeList, complexSqlNodeList, whereExistsCheckList);
        } catch (Exception ex) {
            NotificationUtil.showTip(ex.getMessage(), this.project);
        }
    }

    /**
     * 检测规范
     *
     * @param indexGuideList     命名不规范索引列表
     * @param checkGuideTypeEnum 索引类型
     */
    private void checkGuide(List<TreeNode> indexGuideList, CheckGuideTypeEnum checkGuideTypeEnum) {
        for (ProjectDbConfig dbConfig : projectConfig.getDbConfigs()) {
            DatabaseConfig databaseConfig = ConfigUtil.getDatabaseConfig(project.getBasePath(), dbConfig.getDatabaseType());
            final String indexStartWith = getIndexStartWith(checkGuideTypeEnum, databaseConfig);
            //索引名称不规范
            List<TableIndex> indexNonGuideList = tableIndexList
                    .stream()
                    .filter(t -> t.getNonUnique().equals(checkGuideTypeEnum.getIndex())
                            && !t.getIndexName().toLowerCase().toLowerCase().startsWith(indexStartWith)
                            && t.getDbName().toLowerCase().equals(dbConfig.getDatabaseName().toLowerCase())
                            && !t.getIndexName().toUpperCase().startsWith(databaseConfig.getPrimaryKeyStartWith()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(indexNonGuideList)) {
                return;
            }

            //取表名
            Map<String, List<TableIndex>> tableNameMap = indexNonGuideList
                    .stream()
                    .collect(Collectors.groupingBy(TableIndex::getTableName));
            List<TreeNode> tableTreeNodeList = new ArrayList<>();
            tableNameMap.forEach((k, v) -> {
                List<TreeNode> indexTreeNodeList = new ArrayList<>();
                v.forEach(item -> {
                    if (!databaseConfig.getIngoreIndex().contains(item.getIndexName())) {
                        indexTreeNodeList.add(new TreeNode(item.getIndexName(), null));
                    }
                });
                if (CollectionUtils.isNotEmpty(indexTreeNodeList)) {
                    tableTreeNodeList.add(new TreeNode(k, indexTreeNodeList));
                }
            });
            if (CollectionUtils.isNotEmpty(tableTreeNodeList)) {
                final String indexStartWithNotice = String.format(checkGuideTypeEnum.getNotice(), databaseConfig.getDatabaseType().toUpperCase(), indexStartWith);
                indexGuideList.add(new TreeNode(String.format("%s (%s)", dbConfig.getDatabaseName(), indexStartWithNotice), tableTreeNodeList));
            }
        }
    }

    /**
     * 根据检测类型返回索引命名开头位
     *
     * @param checkGuideTypeEnum 检测索引类型
     * @param databaseConfig     数据配置
     * @return 开头位
     */
    private String getIndexStartWith(CheckGuideTypeEnum checkGuideTypeEnum, DatabaseConfig databaseConfig) {
        final String indexStartWith;
        if (checkGuideTypeEnum.equals(CheckGuideTypeEnum.INDEX_NORMAL_NAME)) {
            indexStartWith = databaseConfig.getNormalIndexNameStartWith();
        } else {
            indexStartWith = databaseConfig.getUniqueIndexNameStartWith();
        }
        return indexStartWith;
    }

    /**
     * 根据所配数据库检测
     *
     * @param fileList                    需要检测的文件列表
     * @param indexGuideNotExistsNodeList 未加索引列表
     * @param complexSqlNodeList          复杂SQL列表
     * @param whereExistsCheckList        未加必加条件列表
     */
    private void checkDbs(List<VirtualFile> fileList,
                          List<TreeNode> indexGuideNotExistsNodeList,
                          List<TreeNode> complexSqlNodeList,
                          List<TreeNode> whereExistsCheckList) {
        for (ProjectDbConfig dbConfig : projectConfig.getDbConfigs()) {
            ConfigUtil.verifyDbConfig(dbConfig);
            DatabaseConfig databaseConfig = ConfigUtil.getDatabaseConfig(project.getBasePath(), dbConfig.getDatabaseType());
            List<VirtualFile> dbFileList = fileList.stream().filter(t -> t.getPath().contains(dbConfig.getMapperPackagePath())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(dbFileList)) {
                List<TreeNode> dbComplexSqlNodeList = new ArrayList<>();
                List<TreeNode> dbWhereExistsCheckList = new ArrayList<>();
                List<TreeNode> dbTree = checkFile(dbFileList, databaseConfig, dbComplexSqlNodeList, dbWhereExistsCheckList);
                if (CollectionUtils.isNotEmpty(dbTree)) {
                    indexGuideNotExistsNodeList.add(new TreeNode(String.format("%s[%s]", dbConfig.getDatabaseName(), databaseConfig.getDatabaseType().toUpperCase()), dbTree));
                }
                if (CollectionUtils.isNotEmpty(dbComplexSqlNodeList)) {
                    final String complexSqlNotice = String.format(CheckGuideTypeEnum.COMPLEX_SQL.getNotice(), databaseConfig.getDatabaseType().toUpperCase(), databaseConfig.getComplexSqlJoinQuantity());
                    complexSqlNodeList.add(new TreeNode(String.format("%s (%s)", dbConfig.getDatabaseName(), complexSqlNotice), dbComplexSqlNodeList));
                }

                if (CollectionUtils.isNotEmpty(dbWhereExistsCheckList)) {
                    final String whereExistsNotice = String.format("%s[%s]", dbConfig.getDatabaseName(), databaseConfig.getDatabaseType().toUpperCase());
                    whereExistsCheckList.add(new TreeNode(whereExistsNotice, dbWhereExistsCheckList));
                }
            }
        }
    }

    /**
     * 检测文件
     *
     * @param fileList               需要检测文件列表
     * @param databaseConfig         数据配置
     * @param dbComplexSqlNodeList   数据库下面复杂SQL列表
     * @param dbWhereExistsCheckList 数据库下未加必要条件列表
     * @return
     */
    private List<TreeNode> checkFile(List<VirtualFile> fileList,
                                     DatabaseConfig databaseConfig,
                                     List<TreeNode> dbComplexSqlNodeList,
                                     List<TreeNode> dbWhereExistsCheckList) {
        List<TreeNode> listTree = new ArrayList<>();

        for (VirtualFile virtualFile : fileList) {
            PsiFile psiFile = PsiManager.getInstance(this.project).findFile(virtualFile);
            try {
                ProjectDbConfig projectDbConfig = ConfigUtil.getDbConfigByFilePath(this.projectConfig, virtualFile.getPath());
                if (projectDbConfig == null) {
                    return null;
                }
                String databaseName = projectDbConfig.getDatabaseName();

                List<TreeNode> columnCheckList = new ArrayList<>();
                List<TreeNode> sqlCheckList = new ArrayList<>();
                List<TreeNode> whereExistsCheckList = new ArrayList<>();
                checkTags(psiFile, databaseName, getConfiguration(psiFile), databaseConfig, columnCheckList, sqlCheckList, whereExistsCheckList);
                if (CollectionUtils.isNotEmpty(columnCheckList)) {
                    listTree.add(new TreeNode(psiFile.getName(), psiFile, true, 1, columnCheckList));
                }
                if (CollectionUtils.isNotEmpty(sqlCheckList)) {
                    dbComplexSqlNodeList.add(new TreeNode(psiFile.getName(), psiFile, true, 1, sqlCheckList));
                }

                if (CollectionUtils.isNotEmpty(whereExistsCheckList)) {
                    dbWhereExistsCheckList.add(new TreeNode(psiFile.getName(), psiFile, true, 1, whereExistsCheckList));
                }
            } catch (Exception ex) {
                listTree.add(new TreeNode(psiFile.getName() + "->error:" + ex.getMessage(), psiFile, false, 1, null));
                dbComplexSqlNodeList.add(new TreeNode(psiFile.getName() + "->error:" + ex.getMessage(), psiFile, false, 1, null));
            }
        }

        return listTree;
    }

    /**
     * 根据文档内容实例化Mybatis Configuration Mybatis为二次开发版
     *
     * @param psiFile PsiFile
     * @return Configuration
     */
    private Configuration getConfiguration(PsiFile psiFile) {
        try {
            Configuration configuration = new Configuration();
            configuration.setDefaultResultSetType(ResultSetType.SCROLL_INSENSITIVE);
            String text = clearInclude(psiFile);
            text = RegexUtil.clearUselessTags(text);
            try (InputStream inputStream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))) {
                XMLMapperBuilder builder = new XMLMapperBuilder(inputStream, configuration, null, configuration.getSqlFragments());
                builder.parse();
            }
            return configuration;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 清理不在同一文件内的Include 替换为 *
     *
     * @param psiFile PsiFile
     * @return Mapper文件内容
     */
    private String clearInclude(PsiFile psiFile) {
        String text = psiFile.getText();
        for (XmlTag xmlTag : ((XmlFileImpl) psiFile).getRootTag().getSubTags()) {
            if (AutoConstant.XML_TAG_TYPE.contains(xmlTag.getName().toLowerCase())) {
                text = clearInclude(psiFile, xmlTag, text);
            }
        }
        return text;
    }

    /**
     * 递归清理 XmlTag 中不在同一文件内的Include 替换为 *
     *
     * @param psiFile PsiFile
     * @param xmlTag  XmlTag
     * @param text    文本
     * @return 处理后的文本
     */
    private String clearInclude(PsiFile psiFile, XmlTag xmlTag, String text) {
        if (xmlTag.getSubTags().length == 0) {
            return text;
        }
        for (XmlTag xmlTagSub : xmlTag.getSubTags()) {
            if (xmlTagSub.getName().equals(AutoConstant.XML_SQL_TAG_INCLUDE) && !isPsiFileRootTag(psiFile, xmlTagSub)) {
                text = text.replace(xmlTagSub.getText(), AutoConstant.SQL_ALL_COLUMN_STAR);
            }
            return clearInclude(psiFile, xmlTagSub, text);
        }
        return text;
    }

    /**
     * Include ID 是否为Mapper根节点
     *
     * @param psiFile PsiFile
     * @param xmlTag  XmlTag
     * @return 结果
     */
    private boolean isPsiFileRootTag(PsiFile psiFile, XmlTag xmlTag) {
        if (xmlTag.getAttribute(AutoConstant.XML_TAG_REF_ID) == null) {
            return false;
        }
        return Arrays.stream(((XmlFileImpl) psiFile).getRootTag().getSubTags())
                .filter(t -> t.getName().equals(AutoConstant.XML_TAG_SQL)
                        && t.getAttribute(AutoConstant.XML_TAG_ID) != null
                        && t.getAttribute(AutoConstant.XML_TAG_ID).getValue().equals(xmlTag.getAttribute(AutoConstant.XML_TAG_REF_ID).getValue()))
                .findFirst()
                .orElse(null) != null;
    }

    /**
     * 检查Mybatis Xml Tag
     *
     * @param psiFile              psiFile
     * @param databaseName         数据库名称
     * @param columnIndexCheckList 未加索引列表
     * @param complexSqlCheckList  复杂SQL列表
     * @param configuration        配置信息
     * @param databaseConfig       数据库信息
     */
    private void checkTags(PsiFile psiFile,
                           String databaseName,
                           Configuration configuration,
                           DatabaseConfig databaseConfig,
                           List<TreeNode> columnIndexCheckList,
                           List<TreeNode> complexSqlCheckList,
                           List<TreeNode> whereExistsCheckList) {
        for (XmlTag xmlTag : ((XmlFileImpl) psiFile).getRootTag().getSubTags()) {
            int lineNumber = StringUtil.getTagLineNumber(psiFile, xmlTag);
            try {
                if (AutoConstant.XML_TAG_TYPE.contains(xmlTag.getName().toLowerCase())) {
                    String xmlSql = xmlTag.getValue().getText();
                    String sql = getCheckSql(xmlTag, configuration);

                    checkSql(xmlSql, sql, psiFile, databaseName, databaseConfig, lineNumber, columnIndexCheckList, complexSqlCheckList, whereExistsCheckList);

                }
            } catch (Exception ex) {
                columnIndexCheckList.add(new TreeNode(String.format("%s 出现异常:%s", xmlTag.getValue().getText(), ex.getMessage()), psiFile, true, lineNumber, null));
                complexSqlCheckList.add(new TreeNode(String.format("%s 出现异常:%s", psiFile.getName(), ex.getMessage()), psiFile, true, lineNumber, null));
            }
        }
    }

    /**
     * 通过修改后的Mybatis提取SQL
     *
     * @param xmlTag        xmlTag
     * @param configuration 配置项
     * @return SQL
     */
    private String getCheckSql(XmlTag xmlTag, Configuration configuration) {
        String id = xmlTag.getAttributeValue(AutoConstant.XML_TAG_ID);
        MappedStatement mappedStatement = configuration.getMappedStatement(id);
        return mappedStatement.getBoundSql(null).getCheckSql();
    }

    /**
     * 检查SQL
     *
     * @param xmlSql               Mybatis SQL
     * @param sql                  提取后的SQL
     * @param psiFile              psiFile
     * @param databaseName         数据库名称
     * @param databaseConfig       数据库配置
     * @param lineNumber           所在文件行号
     * @param columnIndexCheckList 未加索引列表
     * @param complexSqlCheckList  复杂SQL列表
     */
    private void checkSql(String xmlSql,
                          String sql,
                          PsiFile psiFile,
                          String databaseName,
                          DatabaseConfig databaseConfig,
                          int lineNumber,
                          List<TreeNode> columnIndexCheckList,
                          List<TreeNode> complexSqlCheckList,
                          List<TreeNode> whereExistsCheckList) {
        String dbType = databaseConfig.getDatabaseType();
        List<SQLStatement> sqlStatementList = SQLUtils.parseStatements(sql, dbType);
        for (SQLStatement sqlStatement : sqlStatementList) {
            SchemaStatVisitor schemaStatVisitor = buildSchemaStatVisitor(dbType);
            sqlStatement.accept(schemaStatVisitor);
            //检测SQL中未加索引字段
            checkSqlIndex(psiFile, databaseName, schemaStatVisitor, databaseConfig, lineNumber, columnIndexCheckList);
            //检测复杂SQL
            checkComplexSql(psiFile, xmlSql, schemaStatVisitor, databaseConfig, lineNumber, complexSqlCheckList);
            //检测必加WHERE条件
            checkWhereExists(psiFile, sqlStatement, schemaStatVisitor, databaseConfig, lineNumber, whereExistsCheckList);
        }
    }

    /**
     * 检测复杂SQL
     *
     * @param psiFile             psiFile
     * @param xmlSql              Mybatis SQL
     * @param schemaStatVisitor   SQL架构
     * @param databaseConfig      数据库配置
     * @param lineNumber          所在文件行号
     * @param complexSqlCheckList 结果列表
     */
    private void checkComplexSql(PsiFile psiFile,
                                 String xmlSql,
                                 SchemaStatVisitor schemaStatVisitor,
                                 DatabaseConfig databaseConfig,
                                 int lineNumber,
                                 List<TreeNode> complexSqlCheckList) {
        if (schemaStatVisitor.getTables().size() > databaseConfig.getComplexSqlJoinQuantity()) {
            complexSqlCheckList.add(new TreeNode(xmlSql, psiFile, true, lineNumber, null));
        }
    }

    /**
     * 检测SQL中未加索引字段
     *
     * @param psiFile              psiFile
     * @param databaseName         数据库名称
     * @param schemaStatVisitor    SQL架构
     * @param databaseConfig       数据库配置
     * @param lineNumber           所在文件行号
     * @param columnIndexCheckList 结果列表
     */
    private void checkSqlIndex(PsiFile psiFile,
                               String databaseName,
                               SchemaStatVisitor schemaStatVisitor,
                               DatabaseConfig databaseConfig,
                               int lineNumber,
                               List<TreeNode> columnIndexCheckList) {
        List<TableStat.Column> columnList = new ArrayList<>();
        for (TableStat.Column column : schemaStatVisitor.getColumns()) {
            if (!isCheckColumn(columnList, column)) {
                continue;
            }
            if (!isColumnIndex(databaseName, column, databaseConfig)
                    && !column.toString().contains(AutoConstant.UNKNOWN)) {
                if (columnIndexCheckList.stream().filter(t -> t.getNodeName().equals(column.toString())).findFirst().orElse(null) == null) {
                    columnIndexCheckList.add(new TreeNode(column.toString(), psiFile, true, lineNumber, null));
                }
            }
        }
    }

    /**
     * 检测必加WHERE条件
     *
     * @param psiFile              psiFile
     * @param sqlStatement         SQL
     * @param schemaStatVisitor    SQL架构
     * @param databaseConfig       数据库配置
     * @param lineNumber           所在文件行号
     * @param whereExistsCheckList 未加列表
     */
    private void checkWhereExists(PsiFile psiFile,
                                  SQLStatement sqlStatement,
                                  SchemaStatVisitor schemaStatVisitor,
                                  DatabaseConfig databaseConfig,
                                  int lineNumber,
                                  List<TreeNode> whereExistsCheckList) {
        schemaStatVisitor.getTables().forEach((k, v) -> {
            if (AutoConstant.SQL_MODE_CHECK_WHERE.contains(v.toString().toLowerCase())) {
                for (String rw : databaseConfig.getRequiredWhere()) {
                    List<String> rwList = Arrays.asList(rw.split(AutoConstant.SPLIT_DOT)).stream().map(s -> s.toLowerCase()).collect(Collectors.toList());
                    if (schemaStatVisitor.getConditions()
                            .stream()
                            .filter(t -> t.getColumn().getTable().toLowerCase().equals(k.getName().toLowerCase())
                                    && rwList.contains(t.getColumn().getName().toLowerCase()))
                            .findFirst().orElse(null) == null) {
                        String nodeName = String.format(CheckGuideTypeEnum.SQL_WHERE_EXISTS.getNotice(), rw, sqlStatement.toString());
                        whereExistsCheckList.add(new TreeNode(nodeName, psiFile, true, lineNumber, null));
                        break;
                    }
                }
            }
        });

    }

    /**
     * 根据表名取所有索引
     *
     * @param projectConfig 项目配置信息
     * @return 所有索引
     */
    public List<TableIndex> getAllIndex(ProjectConfig projectConfig) {
        if (CollectionUtils.isEmpty(projectConfig.getDbConfigs())) {
            return null;
        }
        List<TableIndex> indexList = new ArrayList<>();
        for (ProjectDbConfig projectDbConfig : projectConfig.getDbConfigs()) {
            DatabaseConfig databaseConfig = ConfigUtil.getDatabaseConfig(project.getBasePath(), projectDbConfig.getDatabaseType());
            String sql = String.format(databaseConfig.getSelectIndexSql(), projectDbConfig.getDatabaseName());
            List<TableIndex> dbIndexList = JdbcUtil.executeDataSql(projectDbConfig, sql, TableIndex.class);
            if (CollectionUtils.isNotEmpty(dbIndexList)) {
                indexList.addAll(dbIndexList);
            }
        }
        return indexList;
    }

    /**
     * 是否已加索引
     *
     * @param databaseName   数据库名称
     * @param column         字段
     * @param databaseConfig 数据配置
     * @return 结果
     */
    public boolean isColumnIndex(String databaseName, TableStat.Column column, DatabaseConfig databaseConfig) {
        if (tableIndexList == null
                || tableIndexList.isEmpty()) {
            return false;
        }
        if (databaseConfig.getIngoreColumn().contains(column.getName())) {
            return true;
        }
        return tableIndexList.stream().filter(t ->
                t.getDbName().toLowerCase().equals(databaseName.toLowerCase())
                        && t.getTableName().toLowerCase().equals(RegexUtil.clearSqlKeyword(column.getTable()).toLowerCase())
                        && t.getColumnName().toLowerCase().equals(RegexUtil.clearSqlKeyword(column.getName()).toLowerCase()))
                .findFirst()
                .orElse(null) != null;
    }

    /**
     * 构建SchemaStatVisitor
     *
     * @param dbType 数据库类型
     * @return SchemaStatVisitor
     */
    private SchemaStatVisitor buildSchemaStatVisitor(String dbType) {
        SchemaStatVisitor schemaStatVisitor = new MySqlSchemaStatVisitor();
        if (DbTypeEnum.SQL_SERVER.getValue().equals(dbType)) {
            schemaStatVisitor = new SQLServerSchemaStatVisitor();
        }
        return schemaStatVisitor;
    }

    /**
     * 是否为需要检测的字段
     *
     * @param columnList 字段列表
     * @param column     字段
     * @return 结果
     */
    private boolean isCheckColumn(List<TableStat.Column> columnList, TableStat.Column column) {
        return (column.isWhere() || column.isJoin()) && !columnList.contains(column.getName())
                && StringUtils.isNotEmpty(RegexUtil.clearSqlKeyword(column.getTable()))
                && StringUtils.isNotEmpty(RegexUtil.clearSqlKeyword(column.getName()))
                && !AutoConstant.UNKNOWN.equals(column.getTable());
    }

}
