package com.autohome.lemon.dbcheck.constant;

/**
 * @author hantianwei
 */
public enum CheckGuideTypeEnum {
    /**
     * UNIQ_NAME
     */
    INDEX_UNIQ_NAME(0, "唯一索引命名不规范", "[%s]唯一索引命名应以 %s 开头"),
    /**
     * NORMAL_NAME
     */
    INDEX_NORMAL_NAME(1, "普通索引命名不规范", "[%s]普通索引命名应以 %s 开头"),
    /**
     * NORMAL_NAME
     */
    COLUMN_NOT_EXISTS(2, "表缺少必加字段", "表缺少必加字段"),
    /**
     * NORMAL_NAME
     */
    INDEX_NOT_EXISTS(3, "查询或关联字段未加索引", "查询或关联字段未加索引"),
    /**
     * NORMAL_NAME
     */
    COMPLEX_SQL(4, "复杂SQL检查", "[%s]表关联超过 %s 个的复杂SQL"),
    /**
     * SQL_WHERE_EXISTS
     */
    SQL_WHERE_EXISTS(4, "条件检查", "[没有必要条件 %s ]%s");

    private String name;

    private int index;

    private String notice;

    CheckGuideTypeEnum(int index, String name, String notice) {
        this.name = name;
        this.index = index;

        this.notice = notice;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }
}
