package io.github.seikodictionaryenginev2.base.util.express.impl;

import io.github.seikodictionaryenginev2.base.util.express.Ref;

import java.util.Map;

/**
 * @Author kagg886
 * @Date 2024/1/22 下午7:12
 * @description:
 */

public class ObjectRef implements Ref {
    private final String key;

    public ObjectRef(String s) {
        this.key = s;
    }

    @Override
    public Object eval(Map<String, Object> data, final Map<String, Object> root) {
        return data.get(key);
    }

    @Override
    public void insert(Map<String, Object> data, Map<String, Object> root, Object value) {
        data.put(key, value);
    }

    @Override
    public String toString() {
        return key;
    }
}
