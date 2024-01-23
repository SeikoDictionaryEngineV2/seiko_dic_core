package io.github.seikodictionaryenginev2.base.util.express.impl;


import io.github.seikodictionaryenginev2.base.util.express.Ref;

import java.util.*;

/**
 * @Author kagg886
 * @Date 2024/1/22 下午7:31
 * @description:
 */

public class FormatRef implements Ref {
    private final String source;
    private String template;
    private List<Ref> val = new ArrayList<>();

    public FormatRef(String template,List<Ref> val) {
        this.template = template;
        this.val = val;
        this.source = String.format(template,val.toArray());
    }

    public FormatRef(String source) {
        this.source = source;
        Map<Integer, Integer> points = new LinkedHashMap<>();

        char[] chr = source.toCharArray();
        int lIndex = -1;
        int deep = 0; //深度
        for (int i = 0; i < chr.length; i++) {
            //遇到Ref头符号则深度+1
            if (chr[i] == '$' && chr[i + 1] == '{' && deep == 0) {
                deep++;
                lIndex = i;
            }

            //遇到Ref终止符号则深度-1
            if (chr[i] == '}') {
                deep--;
                //若深度恰好为0证明一个Ref字符串已解析完毕
                if (deep == 0) {
                    points.put(lIndex, i + 1);
                }
            }
        }


        if (deep!=0) {
            throw new IllegalArgumentException("参数解析失败!原因:发现未闭合的表达式");
        }

        this.template = source;
        //替换
        points.entrySet().stream().map((e) -> source.substring(e.getKey(),e.getValue())).peek((v) -> {
            val.add(Ref.get(v));
        }).forEach((v) -> {
            template = template.replace(v,"%s");
        });
    }

    @Override
    public Object eval(Map<String, Object> data,final Map<String,Object> root) {
        Object[] str = new String[val.size()];

        for (int i = 0; i < val.size(); i++) {
            str[i] = val.get(i).eval(root).toString();
        }
        return String.format(template,str);
    }

    @Override
    public void insert(Map<String, Object> data,Map<String,Object> root ,Object value) {
        throw new UnsupportedOperationException("Not Supported");
    }

    @Override
    public String toString() {
        return source;
    }
}
