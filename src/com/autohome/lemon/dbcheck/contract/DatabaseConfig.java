package com.autohome.lemon.dbcheck.contract;

import java.util.List;

/**
 * @author hantianwei
 */
public class DatabaseConfig {
    private String jdbcDriver;
    private String jdbcUrl;
    private String selectIndexSql;
    private String selectColumnSql;
    private List<CheckGuideExistsColumn> existsColumn;
    private List<String> ingoreColumn;
    private List<String> ingoreIndex;
    private int complexSqlJoinQuantity;
    private String databaseType;
    private String primaryKeyStartWith;
    private String normalIndexNameStartWith;
    private String uniqueIndexNameStartWith;
    private List<String> requiredWhere;

    public String getJdbcDriver() {
        return jdbcDriver;
    }

    public void setJdbcDriver(String jdbcDriver) {
        this.jdbcDriver = jdbcDriver;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getSelectIndexSql() {
        return selectIndexSql;
    }

    public void setSelectIndexSql(String selectIndexSql) {
        this.selectIndexSql = selectIndexSql;
    }

    public String getSelectColumnSql() {
        return selectColumnSql;
    }

    public void setSelectColumnSql(String selectColumnSql) {
        this.selectColumnSql = selectColumnSql;
    }

    public List<CheckGuideExistsColumn> getExistsColumn() {
        return existsColumn;
    }

    public void setExistsColumn(List<CheckGuideExistsColumn> existsColumn) {
        this.existsColumn = existsColumn;
    }

    public List<String> getIngoreColumn() {
        return ingoreColumn;
    }

    public void setIngoreColumn(List<String> ingoreColumn) {
        this.ingoreColumn = ingoreColumn;
    }

    public List<String> getIngoreIndex() {
        return ingoreIndex;
    }

    public void setIngoreIndex(List<String> ingoreIndex) {
        this.ingoreIndex = ingoreIndex;
    }

    public int getComplexSqlJoinQuantity() {
        return complexSqlJoinQuantity;
    }

    public void setComplexSqlJoinQuantity(int complexSqlJoinQuantity) {
        this.complexSqlJoinQuantity = complexSqlJoinQuantity;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    public String getPrimaryKeyStartWith() {
        return primaryKeyStartWith;
    }

    public void setPrimaryKeyStartWith(String primaryKeyStartWith) {
        this.primaryKeyStartWith = primaryKeyStartWith;
    }

    public String getNormalIndexNameStartWith() {
        return normalIndexNameStartWith;
    }

    public void setNormalIndexNameStartWith(String normalIndexNameStartWith) {
        this.normalIndexNameStartWith = normalIndexNameStartWith;
    }

    public String getUniqueIndexNameStartWith() {
        return uniqueIndexNameStartWith;
    }

    public void setUniqueIndexNameStartWith(String uniqueIndexNameStartWith) {
        this.uniqueIndexNameStartWith = uniqueIndexNameStartWith;
    }

    public List<String> getRequiredWhere() {
        return requiredWhere;
    }

    public void setRequiredWhere(List<String> requiredWhere) {
        this.requiredWhere = requiredWhere;
    }
}
