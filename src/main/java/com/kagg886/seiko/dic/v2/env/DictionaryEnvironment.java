package com.kagg886.seiko.dic.v2.env;

import com.kagg886.seiko.dic.v2.entity.code.DictionaryCommandMatcher;
import com.kagg886.seiko.dic.v2.entity.code.func.Function;
import com.kagg886.seiko.util.storage.JSONObjectStorage;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

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
    private DictionaryEnvironment() {}

    public static DictionaryEnvironment getInstance() {
        return INSTANCE;
    }

    public void updateDICConfig(String dicName, Object newConfig) {
        final JSONObjectStorage dicConfig = getDicConfig();
        dicConfig.put(dicName, newConfig);
        dicConfig.save();
    }

    public Map<String,Class<? extends Function>> getGlobalFunctionRegister() {
        return Function.globalManager;
    }
    public Map<String,Class<?>[]> getEventDomain() {
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
