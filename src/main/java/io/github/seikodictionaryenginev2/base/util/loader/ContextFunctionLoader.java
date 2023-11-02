package io.github.seikodictionaryenginev2.base.util.loader;

import io.github.seikodictionaryenginev2.base.entity.code.func.Function;

import java.io.File;
import java.util.HashMap;

/**
 * @projectName: Seiko
 * @package: com.kagg886.seiko.dic
 * @className: ContextFunctionLoader
 * @author: kagg886
 * @description: 伪代码上下文加载器接口
 * @date: 2023/11/2 18:21
 * @version: 1.0
 */
public interface ContextFunctionLoader {

    class NotSupported implements ContextFunctionLoader {

        @Override
        public HashMap<String,Class<? extends Function>> loadClass(File file) {
            return null;
        }
    }


    HashMap<String,Class<? extends Function>> loadClass(File file);
}
