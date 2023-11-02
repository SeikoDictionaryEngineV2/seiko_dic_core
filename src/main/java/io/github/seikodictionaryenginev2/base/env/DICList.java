package io.github.seikodictionaryenginev2.base.env;

import io.github.seikodictionaryenginev2.base.entity.DictionaryFile;
import io.github.seikodictionaryenginev2.base.entity.DictionaryProject;
import io.github.seikodictionaryenginev2.base.model.DICParseResult;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @projectName: Seiko
 * @package: com.kagg886.seiko.dic
 * @className: DICList
 * @author: kagg886
 * @description: 保管了从存储里加载的伪代码文件
 * @date: 2023/1/9 19:33
 * @version: 1.0
 */
public class DICList extends ArrayList<DictionaryProject> {

    public static final DICList INSTANCE = new DICList();


    private DICList() {

    }

    public List<DICParseResult> refresh() {
        clear();
        List<DICParseResult> results = new ArrayList<>();
        for (File p : Objects.requireNonNull(DictionaryEnvironment.getInstance().getDicRoot().listFiles())) {
            DICParseResult result = new DICParseResult();
            result.dicName = p.getName();
            try {
                DictionaryProject project = new DictionaryProject(p);
                project.init();
                add(project);
            } catch (IOException e) {
                result.success = false;
                result.err = e;
            }
            results.add(result);
        }
        return results;
    }

    public List<DictionaryFile> subFiles() {
        return stream().map(DictionaryProject::getIndexFile).collect(Collectors.toList());
    }
}
