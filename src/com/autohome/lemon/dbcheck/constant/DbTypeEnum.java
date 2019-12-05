package com.autohome.lemon.dbcheck.constant;

/**
 * @author hantianwei
 */
public enum DbTypeEnum {
    /**
     * 全量检查
     */
    MYSQL("mysql"),
    /**
     * 增量检查
     */
    SQL_SERVER("sqlserver");

    private String value;

    DbTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
