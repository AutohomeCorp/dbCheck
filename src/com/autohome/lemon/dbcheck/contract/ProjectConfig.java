package com.autohome.lemon.dbcheck.contract;

import java.util.List;

/**
 * @author hantianwei
 */
public class ProjectConfig {
    private String projectName;
    private List<ProjectDbConfig> dbConfigs;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public List<ProjectDbConfig> getDbConfigs() {
        return dbConfigs;
    }

    public void setDbConfigs(List<ProjectDbConfig> dbConfigs) {
        this.dbConfigs = dbConfigs;
    }
}
