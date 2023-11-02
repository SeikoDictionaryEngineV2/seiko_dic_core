package com.kagg886.seiko.dic.v2.env;

import com.kagg886.seiko.dic.v2.entity.DictionaryProject;
import com.kagg886.seiko.dic.v2.entity.code.DictionaryCommandMatcher;
import com.kagg886.seiko.dic.v2.entity.code.func.Function;
import com.kagg886.seiko.dic.v2.model.DictionarySetting;
import com.kagg886.seiko.dic.v2.util.loader.ContextFunctionLoader;
import com.kagg886.seiko.util.storage.JSONObjectStorage;

import java.io.File;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

/**
 * @projectName: Seiko
 * @package: com.kagg886.seiko.dic
 * @className: DictionaryEnvironment
 * @author: kagg886
 * @description: Dictionary所处的环境类
 * @date: 2023/1/27 15:24
 * @version: 1.0
 */
public class DictionaryEnvironment {
    private static final DictionaryEnvironment INSTANCE = new DictionaryEnvironment();
    private String dicConfigPoint; //dicConfig存储路径
    private Path dicData; //dic生成的文件根路径
    private File dicRoot; //dic存储的根目录

    private ContextFunctionLoader contextFunctionLoader = new ContextFunctionLoader.NotSupported(); //上下文方法加载器，根据不同平台实现。若无此加载器则无法解析上下文方法

    private DictionaryEnvironment() {
    }

    public static DictionaryEnvironment getInstance() {
        return INSTANCE;
    }

    public void updateDICConfig(String dicName, Object newConfig) {
        final JSONObjectStorage dicConfig = getDicConfig();
        dicConfig.put(dicName, newConfig);
        dicConfig.save();
    }

    public ContextFunctionLoader getContextFunctionLoader() {
        return contextFunctionLoader;
    }

    public void setContextFunctionLoader(ContextFunctionLoader contextFunctionLoader) {
        this.contextFunctionLoader = contextFunctionLoader;
    }

    public Map<String, Class<? extends Function>> getGlobalFunctionRegister() {
        return Function.globalManager;
    }

    public Map<String, Class<?>[]> getEventDomain() {
        return DictionaryCommandMatcher.domainQuoteNew;
    }

    public File getDicRoot() {
        return dicRoot;
    }

    public void setDicRoot(File dicRoot) {
        this.dicRoot = dicRoot;
    }

    public JSONObjectStorage getDicConfig() {
        return JSONObjectStorage.obtain(dicConfigPoint);
    }

    public DictionarySetting getSetting(DictionaryProject project) {
        if (!getDicList().contains(project)) {
            throw new FileSystemNotFoundException(project.getName() + "未载入DictionaryEnvironment，可能由以下原因构成:\n1. 项目在载入时有语法错误\n2. 这个词库项目是使用构造参数初始化的而不是由DictionaryEnvironment加载");
        }
        return Optional.ofNullable(getDicConfig().getObject(project.getName(), DictionarySetting.class))
                .orElse(DictionarySetting.getDefault());
    }

    public DICList getDicList() {
        return DICList.INSTANCE;
    }

    public Path getDicData() {
        return dicData;
    }

    public void setDicData(Path dicData) {
        this.dicData = dicData;
    }

    public void setDicConfigPoint(String dicConfigPoint) {
        this.dicConfigPoint = dicConfigPoint;
    }
}
