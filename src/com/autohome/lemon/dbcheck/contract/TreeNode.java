package com.autohome.lemon.dbcheck.contract;

import java.util.List;

import com.intellij.psi.PsiFile;

/**
 * @author hantianwei
 */
public class TreeNode {
    private String nodeName;
    private PsiFile psiFile;
    private boolean isTarget;
    private int lineNumber;
    private List<TreeNode> children;
    private boolean hasChildren = false;

    public TreeNode() {

    }

    public TreeNode(String nodeName, List<TreeNode> children) {
        this.nodeName = nodeName;
        this.children = children;
    }

    public TreeNode(String nodeName, PsiFile psiFile, boolean isTarget, int lineNumber, List<TreeNode> children) {
        this.nodeName = nodeName;
        this.psiFile = psiFile;
        this.isTarget = isTarget;
        this.lineNumber = lineNumber;
        this.children = children;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public PsiFile getPsiFile() {
        return psiFile;
    }

    public void setPsiFile(PsiFile psiFile) {
        this.psiFile = psiFile;
    }

    public boolean isTarget() {
        return isTarget;
    }

    public void setTarget(boolean target) {
        isTarget = target;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }

    public boolean hasChildren() {
        return this.children != null && children.size() > 0;
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    @Override
    public String toString() {
        return this.nodeName;
    }
}
