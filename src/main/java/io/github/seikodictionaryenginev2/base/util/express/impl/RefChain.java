package io.github.seikodictionaryenginev2.base.util.express.impl;

import io.github.seikodictionaryenginev2.base.exception.DictionaryOnRunningException;
import io.github.seikodictionaryenginev2.base.util.express.Ref;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author kagg886
 * @Date 2024/1/22 下午6:58
 * @description:
 */

public class RefChain extends ArrayList<Ref> implements Ref {

    public RefChain() {
        super();
    }

    public RefChain(List<Ref> collect) {
        super(collect);
    }

    @SuppressWarnings("all")
    @Override
    public Object eval(Map<String, Object> data, final Map<String, Object> root) {
        Object pointer = data;

        for (Ref ref : this) {
            if (ref instanceof FormatRef) {
                pointer = new ObjectRef(ref.eval(root).toString()).eval(((Map<String, Object>) pointer));
                continue;
            }
            try {
                pointer = ref.eval((Map<String, Object>) pointer, root);
            } catch (NullPointerException e) {
                RefChain c = new RefChain(subList(0, this.indexOf(ref)));
                throw new NullPointerException("表达式计算有误：" + c.toString() + "的结果为null，不可以继续取属性" + ref.toString());
            }
        }
        return pointer == data ? null : pointer;
    }

    @Override
    public void insert(Map<String, Object> data, Map<String, Object> root, Object value) {
        Object pointer = data;
        for (int i = 0; i < this.size() - 1; i++) {
            Ref ref = get(i);
            if (ref instanceof FormatRef) {
                pointer = new ObjectRef(ref.eval(root).toString()).eval(((Map<String, Object>) pointer));
                continue;
            }
            pointer = ref.eval((Map<String, Object>) pointer, root);
        }
        get(size() - 1).insert(((Map<String, Object>) pointer), root, value);
    }

    @Override
    public String toString() {
        return stream().map(Ref::toString).collect(Collectors.joining("."));
    }
}
