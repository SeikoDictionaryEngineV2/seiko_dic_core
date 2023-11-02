package io.github.seikodictionaryenginev2.base.util.express.impl;

import io.github.seikodictionaryenginev2.base.util.express.Ref;

import java.util.Map;

/**
 * 对对象取引用
 *
 * @author kagg886
 * @date 2023/8/5 18:37
 **/
public class ObjectRef implements Ref {
    protected String source;

    public ObjectRef(String s) {
        this.source = s;
    }
    @Override
    public Object get(Map<String, Object> env) {
        return env.getOrDefault(source,null);
    }

    @Override
    public String toString() {
        if (DEBUG) {
            final StringBuilder sb = new StringBuilder("ObjectRef{");
            sb.append("source='").append(source).append('\'');
            sb.append('}');
            return sb.toString();
        } else {
            return source;
        }
    }
}
