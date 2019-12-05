package com.autohome.lemon.dbcheck.util;

import java.util.List;
import java.util.Optional;
import javax.swing.tree.DefaultMutableTreeNode;

import com.autohome.lemon.dbcheck.constant.AutoConstant;
import com.autohome.lemon.dbcheck.contract.TreeNode;
import com.autohome.lemon.dbcheck.ui.ProjectTreeModel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.treeStructure.Tree;

/**
 * 控制台帮助类
 *
 * @author hantianwei
 */
public class ConsoleUtil {

    private static Tree getRootTree(ToolWindow toolWindow) {
        return Optional.ofNullable(toolWindow.getContentManager().getContent(0))
                .map(Content::getComponent)
                .map(item -> (JBScrollPane) item.getComponent(0))
                .map(item -> (Tree) item.getViewport().getComponent(0))
                .orElse(null);
    }

    /**
     * 显示窗口
     *
     * @param project    Project
     * @param toolWindow ToolWindow
     * @param rootNode   节点
     */
    public static void showToolWin(Project project,
                                   ToolWindow toolWindow,
                                   DefaultMutableTreeNode rootNode) {
        Tree rootTree = getRootTree(toolWindow);
        if (rootTree == null) {
            System.err.println("==========> rootTree is null");
            return;
        }
        //构造一个treeModel 对象，进行刷新树操作
        ProjectTreeModel treeModel = new ProjectTreeModel(project, rootNode);
        rootTree.setModel(treeModel);
    }

    /**
     * 显示控制台
     *
     * @param project  Project
     * @param treeList 节点
     */
    public static void showConsole(Project project,
                                   List<TreeNode> treeList) {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(AutoConstant.TITLE);
        for (TreeNode treeNode : treeList) {
            setDefaultMutableTreeNode(treeNode, rootNode);
        }
        ToolWindow toolWindow = getToolWindow(project);
        if (toolWindow == null) {
            System.err.println("==========> toolWindow is null");
            return;
        }
        showToolWin(project, toolWindow, rootNode);
    }

    /**
     * 设置默认节点
     *
     * @param treeNode        节点
     * @param mutableTreeNode 多层节点
     */
    private static void setDefaultMutableTreeNode(TreeNode treeNode, DefaultMutableTreeNode mutableTreeNode) {
        if (treeNode.hasChildren()) {
            DefaultMutableTreeNode cNode = new DefaultMutableTreeNode(treeNode);
            for (TreeNode childrenNode : treeNode.getChildren()) {
                setDefaultMutableTreeNode(childrenNode, cNode);
            }
            mutableTreeNode.add(cNode);
        } else {
            mutableTreeNode.add(new DefaultMutableTreeNode(treeNode));
        }
    }

    /**
     * 取窗体信息
     *
     * @param event 事件
     * @return ToolWindow
     */
    public static ToolWindow getToolWindow(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        return getToolWindow(project);
    }

    /**
     * 取窗体信息
     *
     * @param project Project
     * @return ToolWindow
     */
    public static ToolWindow getToolWindow(Project project) {
        if (project == null) {
            return null;
        }
        return ToolWindowManager.getInstance(project).getToolWindow("AutoDBCheck");
    }

}
