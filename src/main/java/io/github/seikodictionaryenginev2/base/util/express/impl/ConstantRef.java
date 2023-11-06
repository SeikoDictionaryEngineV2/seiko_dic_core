package io.github.seikodictionaryenginev2.base.util.express.impl;

import io.github.seikodictionaryenginev2.base.util.express.Ref;

import java.util.Map;

/**
 * 常量ref
 *
 * @author kagg886
 * @date 2023/8/5 19:23
 **/
public class ConstantRef extends ObjectRef {

    public ConstantRef(String s) {
        super(s);
    }

    @Override
    public Object get(Map<String, Object> env) {
        return source;
    }

    @Override
    public void set(Map<String, Object> env, Object value) {
        throw new UnsupportedOperationException("常量Ref不支持这种操作!");
    }

    @Override
    public String toString() {
        if (Ref.DEBUG) {
            final StringBuilder sb = new StringBuilder("ConstantRef{");
            sb.append("source='").append(source).append('\'');
            sb.append('}');
            return sb.toString();
        } else {
            return source;
        }
    }
}
