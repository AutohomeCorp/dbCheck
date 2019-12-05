package com.autohome.lemon.dbcheck.contract;

/**
 * @author hantianwei
 */
public class DatabaseConfigCustom {
    private DatabaseConfig mysql;
    private DatabaseConfig sqlServer;

    public DatabaseConfig getMysql() {
        return mysql;
    }

    public void setMysql(DatabaseConfig mysql) {
        this.mysql = mysql;
    }

    public DatabaseConfig getSqlServer() {
        return sqlServer;
    }

    public void setSqlServer(DatabaseConfig sqlServer) {
        this.sqlServer = sqlServer;
    }
}
