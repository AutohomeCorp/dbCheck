package com.autohome.lemon.dbcheck.contract;

/**
 * @author hantianwei
 */
public class CheckGuideExistsColumn {
    private String columnName;
    private String dataType;
    private String isIndex;

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getIsIndex() {
        return isIndex;
    }

    public void setIsIndex(String isIndex) {
        this.isIndex = isIndex;
    }
}
