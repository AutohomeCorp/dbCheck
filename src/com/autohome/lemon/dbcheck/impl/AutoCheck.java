package com.autohome.lemon.dbcheck.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.autohome.lemon.dbcheck.constant.AutoConstant;
import com.autohome.lemon.dbcheck.constant.CheckGuideTypeEnum;
import com.autohome.lemon.dbcheck.constant.CheckModeEnum;
import com.autohome.lemon.dbcheck.contract.ProjectConfig;
import com.autohome.lemon.dbcheck.contract.TreeNode;
import com.autohome.lemon.dbcheck.util.ConfigUtil;
import com.autohome.lemon.dbcheck.util.ConsoleUtil;
import com.autohome.lemon.dbcheck.util.DruidUtil;
import com.autohome.lemon.dbcheck.util.JgitUtil;
import com.autohome.lemon.dbcheck.util.NotificationUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.xml.XmlFileImpl;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author hantianwei
 */
public class AutoCheck {

    private List<TreeNode> rootTreeNodeList = new ArrayList<>();
    private List<TreeNode> defaultRootTreeNodeList = new ArrayList<>();

    public static AutoCheck build() {
        return new AutoCheck();
    }

    /**
     * 检查执行
     *
     * @param event         AnActionEvent
     * @param checkModeEnum 检查方式
     */
    public void execute(AnActionEvent event, CheckModeEnum checkModeEnum) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        defaultRootTreeNodeList.add(new TreeNode("Checking...", null));
        if (project == null) {
            System.err.println("==========> project is null");
            return;
        }
        ConsoleUtil.showConsole(project, defaultRootTreeNodeList);

        Task.Backgroundable task = new Task.Backgroundable(project, "AutoDBCheck running") {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                ApplicationManager.getApplication().runReadAction(() -> {
                    executeSync(checkModeEnum, project);
                });
            }
        };
        ProgressManager.getInstance().run(task);
    }

    /**
     * 异步执行
     *
     * @param checkModeEnum 检测类型
     * @param project       Project
     */
    private void executeSync(CheckModeEnum checkModeEnum, Project project) {
        try {
            List<VirtualFile> checkFileList = new ArrayList<>();
            if (checkModeEnum.equals(CheckModeEnum.ALL)) {
                getAllFiles(Arrays.asList(ProjectRootManager.getInstance(project).getContentSourceRoots()), checkFileList);
                checkFileList = getMapperFiles(checkFileList, project);
            } else {
                JgitUtil.init(project.getBasePath());
                checkFileList = getMapperFiles(JgitUtil.getDiffVirtualFileList(), project);
            }
            ProjectConfig projectConfig = ConfigUtil.getConfig(project.getName());
            ConfigUtil.verifyProjectConfig(projectConfig);

            List<TreeNode> indexGuideNotExistsNodeList = new ArrayList<>();
            List<TreeNode> indexGuideUniqNodeList = new ArrayList<>();
            List<TreeNode> indexGuideNormalNodeList = new ArrayList<>();
            List<TreeNode> complexSqlNodeList = new ArrayList<>();
            List<TreeNode> whereExistsCheckList = new ArrayList<>();
            CheckMapper.build(project, projectConfig).execute(checkFileList,
                    indexGuideNotExistsNodeList,
                    indexGuideNormalNodeList,
                    indexGuideUniqNodeList,
                    complexSqlNodeList,
                    whereExistsCheckList);
            addRootTreeNode(indexGuideNotExistsNodeList, CheckGuideTypeEnum.INDEX_NOT_EXISTS);
            addRootTreeNode(indexGuideUniqNodeList, CheckGuideTypeEnum.INDEX_UNIQ_NAME);
            addRootTreeNode(indexGuideNormalNodeList, CheckGuideTypeEnum.INDEX_NORMAL_NAME);
            addRootTreeNode(complexSqlNodeList, CheckGuideTypeEnum.COMPLEX_SQL);
            addRootTreeNode(whereExistsCheckList, CheckGuideTypeEnum.SQL_WHERE_EXISTS);

            List<TreeNode> columnNodeGuideNotExists = new ArrayList<>();
            CheckColumn.build(project, projectConfig).execute(columnNodeGuideNotExists);
            addRootTreeNode(columnNodeGuideNotExists, CheckGuideTypeEnum.COLUMN_NOT_EXISTS);

            if (CollectionUtils.isEmpty(rootTreeNodeList)) {
                rootTreeNodeList.add(new TreeNode("Perfect,请继续保持！", null));
            }
            ConsoleUtil.showConsole(project, rootTreeNodeList);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            NotificationUtil.showTip(ex.getMessage(), project);
            DruidUtil.clearDataSource();
            defaultRootTreeNodeList = new ArrayList<>();
            defaultRootTreeNodeList.add(new TreeNode("Error" + ex.getMessage(), null));
            ConsoleUtil.showConsole(project, defaultRootTreeNodeList);
        }
    }

    public void addRootTreeNode(List<TreeNode> treeList, CheckGuideTypeEnum checkGuideTypeEnum) {
        if (CollectionUtils.isEmpty(treeList)) {
            return;
        }
        rootTreeNodeList.add(new TreeNode(checkGuideTypeEnum.getName(), treeList));
    }


    /**
     * 获取所有文件
     *
     * @param rootDirectory
     * @param allFiles
     */
    public void getAllFiles(List<VirtualFile> rootDirectory, List<VirtualFile> allFiles) {
        if (CollectionUtils.isEmpty(rootDirectory)) {
            return;
        }

        for (VirtualFile virtualFile : rootDirectory) {
            if (virtualFile.isDirectory()) {
                VirtualFile[] children = virtualFile.getChildren();
                getAllFiles(Arrays.asList(children), allFiles);
            } else {
                allFiles.add(virtualFile);
            }
        }
    }

    /**
     * \
     * 找出mapper.xml文件
     *
     * @param files
     */
    public List<VirtualFile> getMapperFiles(List<VirtualFile> files, Project project) {
        if (files == null || files.size() == 0) {
            return null;
        }

        List<VirtualFile> mapperFiles = new ArrayList<>();
        for (VirtualFile file : files) {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            if (isFileMapper(psiFile)) {
                mapperFiles.add(file);
            }

        }
        return mapperFiles;
    }

    /**
     * 判断文件是否为Mapper
     *
     * @param psiFile PsiFile
     * @return 是否
     */
    public boolean isFileMapper(PsiFile psiFile) {
        if (psiFile.getName().endsWith(AutoConstant.XML_FILE_SUFFIX)
                && AutoConstant.MYBATIS_MAPPER_ROOT_TAG.equals(((XmlFileImpl) psiFile).getRootTag().getName())) {
            return true;
        }
        return false;
    }

}
