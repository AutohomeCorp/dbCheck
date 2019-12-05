package com.autohome.lemon.dbcheck.util;

import com.autohome.lemon.dbcheck.constant.AutoConstant;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;

/**
 * 提示信息帮助类
 *
 * @author hantianwei
 */
public class NotificationUtil {

    /**
     * 显示提示信息
     *
     * @param content 提示信息内容
     * @param project Project
     */
    public static void showTip(String content, Project project) {
        Notification notification = new Notification(AutoConstant.GROUP_ID, AutoConstant.TITLE, content, NotificationType.WARNING);
        Notifications.Bus.notify(notification, project);
    }

}
