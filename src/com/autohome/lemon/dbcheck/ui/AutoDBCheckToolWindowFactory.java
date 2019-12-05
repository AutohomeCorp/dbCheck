package com.autohome.lemon.dbcheck.ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

import com.autohome.lemon.dbcheck.contract.TreeNode;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

/**
 * @author wangxiaoming
 * @version 1.0
 * @date 2019-10-22
 */
public class AutoDBCheckToolWindowFactory implements ToolWindowFactory {

    private Content content;
    private JPanel mainPanel;
    private JBScrollPane scrollPane;
    private Tree rootTree;
    private final static int MOUSE_CLICK_COUNT = 2;

    @Override
    public void createToolWindowContent(@NotNull Project project,
                                        @NotNull ToolWindow toolWindow) {
        System.out.println("--------------------> createToolWindowContent");

        mainPanel = new JPanel();
        // 设置布局
        mainPanel.setLayout(new BorderLayout());

        scrollPane = new JBScrollPane();
        //设置滚动条的滚动速度
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        //构造一个treeModel 对象，进行刷新树操作
        rootTree = new Tree();
        // 添加节点双击事件
        rootTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // 双击事件
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == MOUSE_CLICK_COUNT) {
                    System.out.println("--------------------> MouseDoubleClick");
                    Tree tree = (Tree) e.getComponent();
                    //获取点击的tree节点
                    DefaultMutableTreeNode note = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                    if (note != null) {
                        Object[] objects = note.getUserObjectPath();
                        if (objects.length > 1) {
                            TreeNode treeNode = (TreeNode) objects[objects.length - 1];
                            if (!treeNode.isTarget()) {
                                return;
                            }
                            if (treeNode.getPsiFile() != null) {
                                VirtualFile virtualFile = treeNode.getPsiFile().getVirtualFile();
                                //打开文件
                                if (tree.getModel() instanceof ProjectTreeModel) {
                                    Project project = ((ProjectTreeModel) tree.getModel()).getProject();
                                    OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(project, virtualFile);
                                    Editor editor = FileEditorManager.getInstance(project).openTextEditor(openFileDescriptor, true);
                                    if (editor != null) {
                                        CaretModel caretModel = editor.getCaretModel();
                                        LogicalPosition logicalPosition = caretModel.getLogicalPosition();
                                        logicalPosition.leanForward(true);
                                        LogicalPosition logical = new LogicalPosition(treeNode.getLineNumber(), logicalPosition.column);
                                        caretModel.moveToLogicalPosition(logical);
                                        SelectionModel selectionModel = editor.getSelectionModel();
                                        selectionModel.selectLineAtCaret();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
        //将tree添加到滚动条面板上
        scrollPane.setViewportView(rootTree);

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        content = contentFactory.createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    public Content getContent() {
        return content;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JBScrollPane getScrollPane() {
        return scrollPane;
    }

    public Tree getRootTree() {
        return rootTree;
    }
}
