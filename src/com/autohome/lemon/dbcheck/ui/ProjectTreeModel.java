package com.autohome.lemon.dbcheck.ui;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import com.intellij.openapi.project.Project;

/**
 * @author wangxiaoming
 * @version 1.0
 * @date 2019-10-22
 */
public class ProjectTreeModel extends DefaultTreeModel {

    private Project project;

    public ProjectTreeModel(Project project,
                            TreeNode root) {
        super(root);
        this.project = project;
    }

    public ProjectTreeModel(Project project,
                            TreeNode root,
                            boolean asksAllowsChildren) {
        super(root, asksAllowsChildren);
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

}
