package com.autohome.lemon.dbcheck.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.autohome.lemon.dbcheck.constant.AutoConstant;
import org.apache.commons.lang3.StringUtils;

/**
 * 正则帮助类
 *
 * @author hantianwei
 */
public class RegexUtil {

    /**
     * 替换Tag
     *
     * @param value        需要替换的值
     * @param regExValue   表达式
     * @param defaultValue 默认值
     * @return 替换后的值
     */
    public static String replaceAllTag(String value, String regExValue, String defaultValue) {
        Pattern pValue = Pattern.compile(regExValue, Pattern.CASE_INSENSITIVE);
        Matcher mValue = pValue.matcher(value);
        return mValue.replaceAll(StringUtils.isEmpty(defaultValue) ? "" : defaultValue);
    }

    /**
     * 清理XML中无用的节点
     *
     * @param value XmlSql
     * @return 清理后的XmlSql
     */
    public static String clearUselessTags(String value) {
        return replaceAllTag(value, AutoConstant.REG_EX_PARAMS_$, "''");
    }

    /**
     * 清理SQL中关键字特殊符号
     *
     * @param sql SQL
     * @return 清理后的SQL
     */
    public static String clearSqlKeyword(String sql) {
        return sql.replace("`", "")
                .replace("[", "")
                .replace("]", "")
                .replace("'", "");
    }
}
