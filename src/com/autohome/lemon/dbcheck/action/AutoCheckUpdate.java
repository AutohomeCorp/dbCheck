package com.autohome.lemon.dbcheck.action;

import com.autohome.lemon.dbcheck.constant.CheckModeEnum;
import com.autohome.lemon.dbcheck.impl.AutoCheck;
import com.autohome.lemon.dbcheck.util.ConsoleUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.wm.ToolWindow;

/**
 * @author hantianwei
 */
public class AutoCheckUpdate extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        ToolWindow toolWindow = ConsoleUtil.getToolWindow(event);
        if (toolWindow != null) {
            // 无论当前状态为关闭/打开，进行强制打开ToolWindow
            toolWindow.show(() -> {
                AutoCheck.build().execute(event, CheckModeEnum.UPDATE);
            });
        }
    }
}