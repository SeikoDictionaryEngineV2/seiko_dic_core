package com.kagg886.seiko.dic.v2.util.express.impl;

import com.kagg886.seiko.dic.v2.util.express.Ref;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 单元中含别的表达式
 *
 * @author kagg886
 * @date 2023/8/5 19:44
 **/
public class FormatRef implements Ref {
    private final String formatStr;
    private final String source;

    private final List<Ref> args = new ArrayList<>();

    public FormatRef(String source) { //形如a{b}{c}
        this.source = source;
        StringBuilder result = new StringBuilder();
        int braceCount = 0;
        int braceLIndex = 0;

        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c == '{') {
                if (braceCount == 0) {
                    braceLIndex = i;
                }
                braceCount++;
            }

            if (c == '}') {
                braceCount--;
                if (braceCount == 0) {
                    String format = source.substring(braceLIndex, i + 1);
                    args.add(Ref.getRef(format));
                    result.append("%s");
                    continue;
                }
            }

            if (braceCount == 0) {
                result.append(c);
            }
        }

        formatStr = result.toString();
    }

    public FormatRef(String source, String formatStr, List<Ref> args) {
        this.formatStr = formatStr;
        this.source = source;
        this.args.addAll(args);
    }

    @Override
    public Object get(Map<String, Object> env) {
        Object[] args = new String[this.args.size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = this.args.get(i).get(env).toString();
        }
        return String.format(formatStr, args);
    }

    @Override
    public String toString() {
        if (DEBUG) {
            final StringBuilder sb = new StringBuilder("FormatRef{");
            sb.append("formatStr='").append(formatStr).append('\'');
            sb.append(", args=").append(args);
            sb.append('}');
            return sb.toString();
        } else {
            return source;
        }
    }
}
