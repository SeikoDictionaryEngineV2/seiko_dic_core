package io.github.seikodictionaryenginev2.base.util.express.impl;

import io.github.seikodictionaryenginev2.base.util.express.Ref;
import io.github.seikodictionaryenginev2.base.util.express.SettableRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 链条，里面有一个个的ref
 *
 * @author kagg886
 * @date 2023/8/5 18:33
 **/
public class RefChain implements Ref, SettableRef {
    private final List<Ref> elements;

    public RefChain() {
        this.elements = new ArrayList<>();
    }

    private RefChain(List<Ref> elements) {
        this.elements = elements;
    }

    public List<Ref> getElements() {
        return elements;
    }

    @Override
    public Object get(Map<String, Object> env) {
        Object point = env;
        for (int i = 0; i < elements.size(); i++) {
            Ref element = elements.get(i);
            if (element instanceof RefChain) {
                element = new ObjectRef(((String) element.get(env))); //强cast一下
            }

            try {
                point = element.get((Map<String, Object>) point);
            } catch (ClassCastException e) {
                throw new IllegalStateException("无法为非集合对象" + elements.subList(0, i) + "求属性:" + element);
            }
        }
        return point;
    }

    @Override
    public String toString() {
        if (DEBUG) {
            final StringBuilder sb = new StringBuilder("RefChain{");
            sb.append("elements=").append(elements);
            sb.append('}');
            return sb.toString();
        } else {
            StringBuilder builder = new StringBuilder();

            for (Ref a : elements) {
                builder.append(a.toString());
                builder.append('.');
            }

            return "{" + builder.substring(0, builder.length() - 1) + "}";
        }
    }

    @Override
    public void set(Map<String, Object> env, Object value) {
        RefChain chain = new RefChain(elements.subList(0, elements.size() - 1));
        Map<String, Object> map = ((Map<String, Object>) chain.get(env));
        Ref set = elements.get(elements.size() - 1);

        if (!(set instanceof SettableRef)) {
            throw new UnsupportedOperationException("属性" + set + "不可写");
        }

        ((SettableRef) set).set(map, value);
    }
}
