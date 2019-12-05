package com.autohome.lemon.dbcheck.util;

import java.io.File;

/**
 * 文件帮助类
 *
 * @author hantianwei
 */
public class FileUtil {

    /**
     * 取文件最后修改时间
     *
     * @param path 文件路径
     * @return 取后修改时间
     */
    public static long getFileLastModified(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return -1;
        }
        return file.lastModified();
    }
}
