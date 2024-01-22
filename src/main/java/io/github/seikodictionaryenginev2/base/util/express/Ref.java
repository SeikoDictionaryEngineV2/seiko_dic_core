package io.github.seikodictionaryenginev2.base.util.express;

import io.github.seikodictionaryenginev2.base.util.express.impl.ArrayRef;
import io.github.seikodictionaryenginev2.base.util.express.impl.FormatRef;
import io.github.seikodictionaryenginev2.base.util.express.impl.ObjectRef;
import io.github.seikodictionaryenginev2.base.util.express.impl.RefChain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author kagg886
 * @Date 2024/1/22 下午6:32
 * @description: 可以解析以下的字符串：
 * ${a.b.c}
 * ${a.b(0)}
 * ${a(0).b}
 * ${a.${a.b}.c}
 * ${a.${b}.c}
 * ${a.${b}(0).c}
 * ${a.ba${c}.d}
 * ${a.ba${c}(0).d}
 * ${a.ba${c}(${j.k})).d}
 * <p>
 * 每个单元可能包含的类型：
 * 1. 单独的文字，这是ObjectRef
 * 2. 单独的文字后面跟上圆括号，这是ArrayRef。在匹配正确的圆括号后，括号内一律当作FormatRef处理
 * 3.
 */

public interface Ref {

    default Object eval(Map<String,Object> data) {
        return eval(data,data);
    }


    void insert(Map<String, Object> data,final Map<String,Object> root, Object value);

    Object eval(Map<String, Object> data,final Map<String,Object> root);

    default void insert(Map<String, Object> data, Object value) {
        insert(data,data,value);
    }

    static Ref get(String template) {

        //拆解根表达式
        if (template.startsWith("${") && template.endsWith("}")) {
            template = template.substring(2, template.length() - 1);
        }
        char[] chr = template.toCharArray();
        List<StringBuilder> splits = new ArrayList<>();
        StringBuilder buf = new StringBuilder();
        int deep = 0; //深度
        for (int i = 0; i < chr.length; i++) {

            //遇到Ref头符号则深度+1
            if (chr[i] == '$' && chr[i + 1] == '{') {
                deep++;
            }

            //遇到Ref终止符号则深度-1
            if (chr[i] == '}') {
                deep--;
            }

            //遇到分割符号且深度为0，清空临时缓冲区。
            if (chr[i] == '.' && deep == 0) {
                splits.add(buf);
                buf = new StringBuilder();
                continue;
            }

            //将字符添加到缓冲区中。
            buf.append(chr[i]);
        }

        //循环终止后缓冲区可能不为空
        if (!buf.isEmpty()) {
            splits.add(buf);
        }

        //第二步：组装Ref
        RefChain c = new RefChain(
                splits.stream().map(StringBuilder::toString)
                        .map(Ref::resolveNewRef)
                        .collect(Collectors.toList()));
        return c.size() == 1 ? c.get(0) : c;
    }

    private static Ref resolveNewRef(String s) {
        if (s.endsWith(")")) { //先匹配内部含有()，按ArrayRef处理
            return new ArrayRef(s);
        }

        if (s.contains("}")) { //再匹配内部含有${}，按FormatRef处理
            return new FormatRef(s);
        }

        //若以上两种都不是，则这是ObjectRef
        return new ObjectRef(s);
    }
}
