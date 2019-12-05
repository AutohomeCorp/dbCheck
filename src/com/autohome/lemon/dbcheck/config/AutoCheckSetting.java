package com.autohome.lemon.dbcheck.config;

import java.awt.*;
import javax.swing.*;

import com.autohome.lemon.dbcheck.util.DruidUtil;
import com.intellij.openapi.options.Configurable;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;


/**
 * @author hantianwei
 */
public class AutoCheckSetting implements Configurable {
    private JPanel yApi;
    private JTextArea config;
    private PersistentState persistentState = PersistentState.getInstance();


    @Override
    @Nls(capitalization = Nls.Capitalization.Title)
    public String getDisplayName() {
        return "Autohome DB Check";
    }


    @Override
    @Nullable
    public JComponent createComponent() {
        this.yApi = new JPanel();
        this.yApi.setLayout(new GridLayoutManager(12, 2));
        //配置title
        JLabel label = new JLabel();
        label.setText("config");
        this.yApi.add(label, new GridConstraints(1, 0, 1, 1, 8, 0, 0, 0, null, null, null, 0, false));
        //配置content
        this.config = new JTextArea();
        this.config.setText(StringUtils.isNotBlank(this.persistentState.getConfig()) ? this.persistentState.getConfig() : getDefaultConfig());
        this.yApi.add(this.config, new GridConstraints(1, 1, 1, 1, 0, 3, 4, 4, null, new Dimension(150, 50), null, 0, false));
        //配置项说明
        JLabel label1 = new JLabel();
        label1.setText("配置说明");
        this.yApi.add(label1, new GridConstraints(2, 0, 10, 1, 8, 0, 0, 0, null, null, null, 0, false));
        //模块名称设置
        JLabel label2 = new JLabel();
        label2.setText("projectName：项目名称,区分大小写。");
        this.yApi.add(label2, new GridConstraints(2, 1, 1, 1, 8, 0, 0, 0, null, null, null, 0, false));
        //url配置
        JLabel label3 = new JLabel();
        label3.setText("url：数据库IP:PORT 例：127.0.0.1:3306");
        this.yApi.add(label3, new GridConstraints(3, 1, 1, 1, 8, 0, 0, 0, null, null, null, 0, false));
        //userName配置
        JLabel label4 = new JLabel();
        label4.setText("username：数据库用户名 例：root");
        this.yApi.add(label4, new GridConstraints(4, 1, 1, 1, 8, 0, 0, 0, null, null, null, 0, false));
        //passWord配置
        JLabel label5 = new JLabel();
        label5.setText("password：数据库密码 例：123456");
        this.yApi.add(label5, new GridConstraints(5, 1, 1, 1, 8, 0, 0, 0, null, null, null, 0, false));
        //databaseName配置
        JLabel label6 = new JLabel();
        label6.setText("databaseName：数据库名");
        this.yApi.add(label6, new GridConstraints(6, 1, 1, 1, 8, 0, 0, 0, null, null, null, 0, false));
        //mapperPackagePath
        JLabel label7 = new JLabel();
        label7.setText("mapperPackagePath：数据库对应mapper文件包的绝对路径,用于区分多库，只要可区分就可以 例：/mapper/");
        this.yApi.add(label7, new GridConstraints(7, 1, 1, 1, 8, 0, 0, 0, null, null, null, 0, false));
        //databaseType
        JLabel label8 = new JLabel();
        label8.setText("databaseType：数据库类型：MYSQL，SQLSERVER  默认值为MYSQL 不区分大小写");
        this.yApi.add(label8, new GridConstraints(8, 1, 1, 1, 8, 0, 0, 0, null, null, null, 0, false));
        return this.yApi;
    }


    @Override
    public boolean isModified() {
        if (StringUtils.isBlank(this.config.getText())) {
            return false;
        }
        if (StringUtils.isBlank(this.persistentState.getConfig())) {
            DruidUtil.clearDataSource();
            return true;
        }
        if ((this.persistentState.getConfig().hashCode() != this.config.getText().hashCode())) {
            DruidUtil.clearDataSource();
            return true;
        }
        return false;
    }


    @Override
    public void apply() {
        this.persistentState.setConfig(this.config.getText());
    }


    private String getDefaultConfig() {
        return "[\n" +
                "    {\n" +
                "        \"projectName\":\"projectName1\",\n" +
                "        \"dbConfigs\":[\n" +
                "            {\n" +
                "                \"url\":\"jdbc:mysql://127.0.0.1:3306/databaseName?useSSL=false&verifyServerCertificate=false\",\n" +
                "                \"userName\":\"root\",\n" +
                "                \"password\":\"123456\",\n" +
                "                \"databaseName\":\"databaseName\",\n" +
                "                \"databaseType\": 0,\n" +
                "                \"mapperPackagePath\":\"/mapper/\"\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"projectName\":\"projectName2\",\n" +
                "        \"dbConfigs\":[\n" +
                "            {\n" +
                "                \"url\":\"jdbc:mysql://127.0.0.1:3306/databaseName?useSSL=false&verifyServerCertificate=false\",\n" +
                "                \"userName\":\"root\",\n" +
                "                \"password\":\"123456\",\n" +
                "                \"databaseName\":\"databaseName\",\n" +
                "                \"databaseType\": 0,\n" +
                "                \"mapperPackagePath\":\"/mapper/\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "]";
    }
}
