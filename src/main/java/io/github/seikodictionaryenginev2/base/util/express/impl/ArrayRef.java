package io.github.seikodictionaryenginev2.base.util.express.impl;


import io.github.seikodictionaryenginev2.base.util.express.Ref;

import java.util.List;
import java.util.Map;

/**
 * @Author kagg886
 * @Date 2024/1/22 下午7:31
 * @description:
 */

public class ArrayRef implements Ref {
    private final String source;
    private final Ref arr;
    private final Ref index;

    private ArrayRef(Ref arr, Ref index) {
        this.source = arr.toString() + "(" + index.toString() + ")";
        this.arr = arr;
        this.index = index;
    }

    public ArrayRef(String template) {
        this.source = template;

        char[] s = template.toCharArray();

        int deep = 0;
        int thesis = -1;
        for (int i = s.length - 1; i >= 0; i--) {
            if (s[i] == ')') {
                deep++;
            }
            if (s[i] == '(') {
                deep--;
                if (deep == 0) {
                    thesis = i;
                }
            }
        }
        String index = template.substring(thesis + 1, template.length() - 1);
        String arr = template.substring(0, thesis); //先FormatRef，得到的结果当作ObjectRef

        this.arr = Ref.get("${" + arr + "}");

        //index不可能是除了ObjectRef以外的Ref
        this.index = index.startsWith("${") && index.endsWith("}") ? new ObjectRef(index.substring(2, index.length() - 1)) : new Ref() {
            @Override
            public Object eval(Map<String, Object> data, Map<String, Object> root) {
                return index;
            }

            @Override
            public void insert(Map<String, Object> data, Map<String, Object> root, Object value) {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public Object eval(Map<String, Object> data, final Map<String, Object> root) {
        Object o;
        if (arr instanceof FormatRef) {
            o = new ObjectRef(arr.eval(data).toString()).eval(root);
        } else {
            o = arr.eval(data);
        }

        if (!(o instanceof List<?>)) {
            throw new IllegalStateException(arr + "的类型不是列表，无法对其运用()进行取值");
        }
        return ((List<?>) o).get(Integer.parseInt(index.eval(root).toString()));
    }

    @Override
    public void insert(Map<String, Object> data, Map<String, Object> root, Object value) {
        List<Object> o;
        if (arr instanceof FormatRef) {
            o = ((List<Object>) new ObjectRef(arr.eval(data).toString()).eval(root));
        } else {
            o = (List<Object>) arr.eval(data);
        }

        //a(0)：替换下标为0的内容
        //a(add)：在末尾添加
        //a(add1)：在下标为1后添加

        String cmd = index.eval(root).toString();

        if (cmd.startsWith("add")) {
            try {
                int pos = Integer.parseInt(cmd.substring(3));
                o.add(pos, value);
            } catch (Exception ignored) {
                o.add(value);
            }
            return;
        }

        try {
            int pos = Integer.parseInt(cmd);
            o.set(pos, value);
        } catch (Exception ignored) {
            throw new IllegalArgumentException("不合法的下标:" + cmd);
        }
    }
}