package io.github.seikodictionaryenginev2.base.util.express.impl;


import io.github.seikodictionaryenginev2.base.util.express.Ref;

import java.util.List;
import java.util.Map;

/**
 * 数组ref
 *
 * @author kagg886
 * @date 2023/8/5 19:11
 **/
public class ArrayRef implements Ref {

    private Ref arr;
    private Ref index;

    public ArrayRef(String source) { //形如{k.x}({b.c(1)})

        int braceLen = 0;
        int parenthesisLen = 0;
        StringBuilder buf = new StringBuilder();
        for (int i = source.length() - 1; i >= 0; i--) {
            char c = source.charAt(i);

            if (c == '}') braceLen++;
            if (c == '{') braceLen--;
            if (c == ')') parenthesisLen++;
            if (c == '(') parenthesisLen--;

            buf.append(c);

            if (parenthesisLen == 0 && braceLen == 0) {
                String index = buf.reverse().substring(1, buf.length() - 1);
                try {
                    this.index = Ref.getRef(index);
                } catch (IllegalArgumentException e) {
                    this.index = new ConstantRef(index);
                }
                this.arr = Ref.getRef("{" + source.substring(0, i) + "}");
                return;
            }
        }
    }

    @Override
    public Object get(Map<String, Object> env) {
        List<Object> objects;


        Object listOfStr = arr.get(env); //ref的计算结果可能是字符串
        if (listOfStr instanceof String) {
            objects = (List<Object>) new ObjectRef((String) listOfStr).get(env);
        } else {
            objects = (List<Object>) arr.get(env);
        }

        if (objects == null) {
            throw new IllegalStateException("对象:" + arr.toString() + "不是列表");
        }

        int index0;
        try {
            index0 = Integer.parseInt(index.get(env).toString());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("传入的下标:" + index.toString() + "不是数字");
        }
        return objects.get(index0);
    }

    @Override
    public String toString() {
        if (DEBUG) {
            final StringBuilder sb = new StringBuilder("ArrayRef{");
            sb.append("arr=").append(arr);
            sb.append(", index=").append(index);
            sb.append('}');
            return sb.toString();
        } else {
            return String.format("%s(%s)", arr.toString(), index.toString());
        }
    }
}
