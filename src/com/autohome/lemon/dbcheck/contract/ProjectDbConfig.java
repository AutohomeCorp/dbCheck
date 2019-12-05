package com.autohome.lemon.dbcheck.contract;

/**
 * @author hantianwei
 */
public class ProjectDbConfig {
    private String url;
    private String userName;
    private String password;
    private String databaseName;
    private String databaseType = "mysql";
    private String mapperPackagePath;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    public String getMapperPackagePath() {
        return mapperPackagePath;
    }

    public void setMapperPackagePath(String mapperPackagePath) {
        this.mapperPackagePath = mapperPackagePath;
    }
}
