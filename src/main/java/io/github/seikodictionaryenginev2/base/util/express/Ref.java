package io.github.seikodictionaryenginev2.base.util.express;


import io.github.seikodictionaryenginev2.base.util.express.impl.ArrayRef;
import io.github.seikodictionaryenginev2.base.util.express.impl.FormatRef;
import io.github.seikodictionaryenginev2.base.util.express.impl.ObjectRef;
import io.github.seikodictionaryenginev2.base.util.express.impl.RefChain;

import java.util.Map;

public interface Ref {

    boolean DEBUG = true;

    Object get(Map<String, Object> env);

    static Ref getRef(String s) {
        if (s.startsWith("{") && s.endsWith("}")) {
            s = s.substring(1, s.length() - 1);

            int braceCount = 0;
            StringBuilder buf = new StringBuilder();
            RefChain chain = new RefChain();

            for (char c : s.toCharArray()) {
                if (c == '{') braceCount++;
                if (c == '}') braceCount--;
                if (c == '.' && braceCount == 0) { //证明一个子表达式收集完毕了，可以进行转换
                    collect(buf, chain);
                    continue;
                }
                buf.append(c);
            }

            if (buf.length() != 0) {
                collect(buf, chain);
            }
            return chain.getElements().size() == 1 ? chain.getElements().get(0) : chain;
        }
        throw new IllegalArgumentException("表达式计算有误");
    }

    private static void collect(StringBuilder buffer, RefChain chain) {
        String buf = buffer.toString();
        if (buf.startsWith("{") && buf.endsWith("}")) {
            chain.getElements().add(getRef(buf));
        } else if (buf.matches(".*\\(.*\\)")) {
            chain.getElements().add(new ArrayRef(buf));
        } else if (buf.contains("{") && buf.contains("}")) {
            chain.getElements().add(new FormatRef(buf));
        } else {
            chain.getElements().add(new ObjectRef(buf));
        }
        buffer.setLength(0);
    }
}
