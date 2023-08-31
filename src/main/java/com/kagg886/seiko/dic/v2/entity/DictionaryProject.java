package com.kagg886.seiko.dic.v2.entity;

import com.kagg886.seiko.dic.v2.entity.code.func.Function;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * dic工程
 *
 * @author kagg886
 * @date 2023/8/30 10:48
 **/
public class DictionaryProject {
    //这个词库的路径。若为单文件则为词库本身，若为文件夹则为内部的index.txt
    private File rootFile;
    //主词库文件。只有主词库才能被用户触发
    private DictionaryFile indexFile;

    //是否是单文件词库
    private boolean isSimpleDictionary = false;

    //词库的子词库列表
    private final List<DictionaryFile> subFile = new ArrayList<>();

    //词库导入的自定义函数
    private final HashMap<String, Class<? extends Function>> imports = new HashMap<>();

    public DictionaryProject(File rootFile) throws IOException {
        this.rootFile = rootFile;
        //支持单词库加载
        isSimpleDictionary = !rootFile.isDirectory();
        initContextFunctions();
    }

    //解析上下文方法
    private void initContextFunctions() {
        File f = rootFile.toPath().resolve("func").toFile();
        for (File jarOrDex : Objects.requireNonNull(f.listFiles())) {
            System.out.println("decode:" + jarOrDex.getName());
        }
    }

    public void init() throws IOException {
        if (isSimpleDictionary) {
            indexFile = new DictionaryFile(this, rootFile);
            indexFile.parseDICCodeFile();
            return;
        }

        for (File file : Objects.requireNonNull(rootFile.listFiles())) {
            if (file.isDirectory()) {
                continue;
            }
            if (file.getName().equals("index.txt")) {
                indexFile = new DictionaryFile(this, file);
                indexFile.parseDICCodeFile();
                continue;
            }
            DictionaryFile sub = new DictionaryFile(this, file);
            sub.parseDICCodeFile();
            subFile.add(sub);
        }

        if (indexFile == null) {
            throw new IOException(String.format("词库'%s'未包括'index.txt',请在'%s'目录下创建index.txt文件!", rootFile.getName(), rootFile.getName()));
        }
    }

    public String getName() {
        return rootFile.getName();
    }

    public List<DictionaryFile> getSubFile() {
        return subFile;
    }

    public DictionaryFile getIndexFile() {
        return indexFile;
    }

    public boolean isSimpleDictionary() {
        return isSimpleDictionary;
    }

    public HashMap<String, Class<? extends Function>> getImports() {
        return imports;
    }
}
