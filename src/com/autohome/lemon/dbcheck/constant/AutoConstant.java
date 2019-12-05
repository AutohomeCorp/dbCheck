package com.autohome.lemon.dbcheck.constant;

import java.util.ArrayList;
import java.util.List;

/**
 * 静态变量
 *
 * @author hantianwei
 */
public class AutoConstant {

    /**
     * 需要检测的节点名称
     */
    public static final List<String> XML_TAG_TYPE = new ArrayList<>();

    /**
     * 需要检测WHERE条件的节点名称
     */
    public static final List<String> SQL_MODE_CHECK_WHERE = new ArrayList<>();

    /**
     * 需要检测的MODE
     */
    public static final List<String> SQL_MODE_CHECK = new ArrayList<>();

    static {
        XML_TAG_TYPE.add("update");
        XML_TAG_TYPE.add("select");
        XML_TAG_TYPE.add("delete");

        SQL_MODE_CHECK_WHERE.add("select");

        SQL_MODE_CHECK.add("update");
        SQL_MODE_CHECK.add("select");
        SQL_MODE_CHECK.add("delete");
    }

    /**
     * Group ID
     */
    public static final String GROUP_ID = "AutoDBCheck.Group";

    /**
     * 提示Title
     */
    public static final String TITLE = "AutoDBCheck";

    public static final String CONFIGURE_ITEM_INCOMPLETE = "配置项不全，请检查配置";

    public static final String MYBATIS_MAPPER_ROOT_TAG = "mapper";

    public static final String XML_FILE_SUFFIX = ".xml";

    public static final String CUSTOM_CONFIG_PATH = "AutoDBCheck.yml";

    public static final String XML_SQL_TAG_INCLUDE = "include";

    public static final String SQL_ALL_COLUMN_STAR = "*";

    public static final String XML_TAG_REF_ID = "refid";

    public static final String XML_TAG_SQL = "sql";

    public static final String XML_TAG_ID = "id";

    public static final String UNKNOWN = "UNKNOWN";

    public static final String REG_EX_PARAMS_$ = "\\$\\{([^}]*)\\}";

    public static final String DATABASE_CONF_PATH = "%s.yml";

    public static final String SPLIT_DOT = ",";
}
