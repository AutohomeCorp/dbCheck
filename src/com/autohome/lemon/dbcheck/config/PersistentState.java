package com.autohome.lemon.dbcheck.config;

import java.util.Objects;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;


/**
 * @author hantianwei
 */
@State(name = "AutoDBCheckConfig", storages = {@Storage(value = "AutoDBCheckConfig.xml")})
public class PersistentState extends Object implements PersistentStateComponent<Element> {
    private String config;

    public static PersistentState getInstance() {
        return (PersistentState) ServiceManager.getService(PersistentState.class);
    }


    @Override
    @Nullable
    public Element getState() {
        Element element = new Element("AutoDBCheckConfig");
        element.setAttribute("config", getConfig());
        return element;
    }


    @Override
    public void loadState(Element element) {
        this.config = element.getAttributeValue("config");
    }


    public String getConfig() {
        return Objects.isNull(this.config) ? "" : this.config;
    }


    public void setConfig(String config) {
        this.config = config;
    }
}
