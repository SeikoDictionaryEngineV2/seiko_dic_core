package com.kagg886.seiko.dic.v2.util.express.impl;

import com.kagg886.seiko.dic.v2.util.express.Ref;

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
