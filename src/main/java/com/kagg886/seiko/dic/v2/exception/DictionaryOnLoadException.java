package com.kagg886.seiko.dic.v2.exception;

/**
 * @projectName: Seiko
 * @package: com.kagg886.seiko.dic.exception
 * @className: DictionaryFileEmptyException
 * @author: kagg886
 * @description: 加载伪代码出错时抛出
 * @date: 2023/1/11 15:19
 * @version: 1.0
 */
public class DictionaryOnLoadException extends RuntimeException {
    public DictionaryOnLoadException(String ss) {
        super(ss);
    }

    public DictionaryOnLoadException(String ss, Throwable e) {
        super(ss, e);
    }
}
