package com.autohome.lemon.dbcheck.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.autohome.lemon.dbcheck.constant.DiffInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;

/**
 * 本地文件和git HEAD 差异工具类
 *
 * @author : zhaofaxian
 * @date : 2019/9/30 15:24
 */
public class JgitUtil {
    private static volatile String PATH;
    private static Git git;

    /**
     * 初始化git
     *
     * @param path 项目路径
     */
    public static void init(String path) {
        try {
            if (git == null) {
                synchronized (JgitUtil.class) {
                    if (git == null) {
                        JgitUtil.PATH = path;
                        File root = new File(path);
                        git = Git.open(root);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取暂存区差异详情
     *
     * @param isContext 是否有上下文
     * @return List<DiffInfo>
     */
    public static List<DiffInfo> getDiffDetailList(boolean isContext) {
        List<DiffInfo> reList = new ArrayList<>();
        // 格式化设置
        ByteArrayOutputStream out = null;
        try {
            List<DiffEntry> workList = getWorkDiff();
            AddCommand addCommand = git.add();
            workList.forEach(k -> addCommand.addFilepattern(k.getNewPath()));
            if (workList.size() > 0) {
                addCommand.call();
            }
            out = new ByteArrayOutputStream();
            DiffFormatter df = new DiffFormatter(out);
            df.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
            df.setRepository(git.getRepository());
            // 获取修改文件
            List<DiffEntry> diffList = getStageDiff();
            for (DiffEntry diffEntry : diffList) {
                StringBuilder stringBuilder = new StringBuilder();
                try {
                    if (isContext) {
                        out.reset();
                        df.format(diffEntry);
                        stringBuilder.append(out.toString("UTF-8"));
                    } else {
                        // 设置内容
                        setDiffContent(stringBuilder, df, out, diffEntry);
                    }
                    DiffInfo diffInfo = new DiffInfo();
                    diffInfo.setChangeType(diffEntry.getChangeType());
                    diffInfo.setContent(stringBuilder.toString());
                    diffInfo.setNewPath(diffEntry.getNewPath());
                    diffInfo.setOldPath(diffEntry.getOldPath());
                    reList.add(diffInfo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (GitAPIException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return reList;
    }

    /**
     * 设置stringBuilder内容
     *
     * @param stringBuilder 返回内容
     * @param df            格式化
     * @param out           缓冲区
     * @param diffEntry     diff文件
     */
    private static void setDiffContent(StringBuilder stringBuilder, DiffFormatter df, ByteArrayOutputStream out, DiffEntry diffEntry) {
        BufferedReader in = null;
        String code;
        try {
            out.reset();
            df.format(diffEntry);
            in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(out.toByteArray())));
            while ((code = in.readLine()) != null) {
                boolean condition = (code.startsWith("+") || code.startsWith("-")) && !(code.startsWith("+++") || code.startsWith("---"));
                if (condition) {
                    stringBuilder.append(code).append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取工作区变化
     *
     * @return List<DiffEntry>
     */
    private static List<DiffEntry> getWorkDiff() {
        try {
            return git.diff().setShowNameAndStatusOnly(true).call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * 获取暂存区变化
     *
     * @return List<DiffEntry>
     */
    private static List<DiffEntry> getStageDiff() {
        try {
            return git.diff().setCached(true).setShowNameAndStatusOnly(true).call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * 获取工作区变化
     *
     * @return List<DiffEntry>
     */
    private static List<DiffEntry> getWordDiff() {
        try {
            return git.diff().setShowNameAndStatusOnly(true).call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * 获取虚拟文件路径
     *
     * @return List<VirtualFile>
     */
    public static List<VirtualFile> getDiffVirtualFileList() {
        List<DiffEntry> workList = getWordDiff();
        List<DiffEntry> stageList = getStageDiff();
        workList.addAll(stageList);
        Map<String, DiffEntry> diffMap =
                workList.stream().filter(k -> (DiffEntry.ChangeType.ADD.equals(k.getChangeType()) || DiffEntry.ChangeType.MODIFY.equals(k.getChangeType())))
                        .collect(Collectors.toMap(DiffEntry::getNewPath, item -> item, (v1, v2) -> v1));

        return diffMap.values().stream().map(k -> LocalFileSystem.getInstance().findFileByIoFile(new File(JgitUtil.PATH + "//" + k.getNewPath()))
        ).collect(Collectors.toList());
    }
}
