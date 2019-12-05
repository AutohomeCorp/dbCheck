package com.autohome.lemon.dbcheck.constant;

import org.eclipse.jgit.diff.DiffEntry;

/**
 * @author : zhaofaxian
 * @date : 2019/10/8 17:27
 */
public class DiffInfo {
    /**
     * 修改内容
     */
    private String content;
    /**
     * 旧文件名
     */
    private String oldPath;
    /**
     * 新文件名
     */
    private String newPath;
    /**
     * 修改类型 新增：ADD 修改：MODIFY 删除DELETE
     */
    private DiffEntry.ChangeType changeType;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getOldPath() {
        return oldPath;
    }

    public void setOldPath(String oldPath) {
        this.oldPath = oldPath;
    }

    public String getNewPath() {
        return newPath;
    }

    public void setNewPath(String newPath) {
        this.newPath = newPath;
    }

    public DiffEntry.ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(DiffEntry.ChangeType changeType) {
        this.changeType = changeType;
    }

}
