package com.autohome.lemon.dbcheck.util;

import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlTag;
import org.apache.commons.lang3.StringUtils;

/**
 * String 帮助类
 *
 * @author hantianwei
 */
public class StringUtil {

    /**
     * 取 XmlTag在PsiFile中的行号
     *
     * @param psiFile PsiFile
     * @param xmlTag  XmlTag
     * @return 所在行号
     */
    public static int getTagLineNumber(PsiFile psiFile, XmlTag xmlTag) {
        int lineNumber = 0;
        String[] split = psiFile.getText().split("\n");
        for (int i = 0; i < split.length; i++) {
            String line = split[i].trim();
            if (StringUtils.isNotEmpty(line)
                    && line.equals(xmlTag.getText().split("\n")[0].trim())) {
                lineNumber = i;
                break;
            }
        }
        return lineNumber + 1;
    }
}
