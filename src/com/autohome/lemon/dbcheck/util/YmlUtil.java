package com.autohome.lemon.dbcheck.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;

/**
 * YML文件读取类
 *
 * @author hantianwei
 */
public class YmlUtil {
    private static final YAMLFactory YAML_FACTORY;
    private static final ObjectMapper MAPPER;

    static {
        YAML_FACTORY = new YAMLFactory();
        MAPPER = new ObjectMapper();
        MAPPER.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /**
     * 根据 InputStream 读取配置
     *
     * @param input InputStream
     * @param klass 类型
     * @param <T>   泛型
     * @return 配置信息
     */
    private static <T> T build(InputStream input, Class<T> klass) {
        try {
            YAMLParser yamlParser = YAML_FACTORY.createParser(input);
            final JsonNode node = MAPPER.readTree(yamlParser);
            TreeTraversingParser treeTraversingParser = new TreeTraversingParser(node);
            final T config = MAPPER.readValue(treeTraversingParser, klass);
            return config;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * buile本地配置
     *
     * @param path  路径
     * @param klass 类型
     * @param <T>   泛型
     * @return 配置信息
     */
    public static <T> T buildLocal(String path, Class<T> klass) {
        InputStream input = YmlUtil.class.getClassLoader().getResourceAsStream(path);
        return build(input, klass);
    }

    /**
     * 根据路径读取YML
     *
     * @param path  路径
     * @param klass 类型
     * @param <T>   泛型
     * @return 配置信息
     */
    public static <T> T build(String path, Class<T> klass) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        try (InputStream input = new FileInputStream(file)) {
            return build(input, klass);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
