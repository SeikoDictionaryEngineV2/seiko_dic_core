package io.github.seikodictionaryenginev2.base.util.calc;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import io.github.seikodictionaryenginev2.base.util.express.Ref;
import io.github.seikodictionaryenginev2.base.util.express.impl.FormatRef;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ref的增强版，在Ref的基础上允许嵌套方括号算术表达式
 * <p>
 * 1. $[]数学表达式  --> Type.MATH
 * 2. {},[]等JSON块 --> Type.JSON
 * 3. ${} -->Type.REF
 * 4. abc${a.b($[1+2])} -->Type.FMT_A
 * 5. abc$[1+${a}] -->Type.FMT_B
 *
 * @author kagg886
 * @date 2023/8/28 20:43
 **/
public class ComputeText implements Ref {
    private String source;
    private Type type;
    private Ref mod;


    //为FMT_A和FMT_B时的格式化参数
    private String template;
    private List<Ref> args = new ArrayList<>();

    public ComputeText(String source) {
        this.source = source;

        try {
            JSON.parse(source);
            type = Type.JSON;
            return;
        } catch (JSONException ignored) {
        }

        if (source.startsWith("$[") && source.endsWith("]") && !source.contains("${")) {
            type = Type.MATH;
            return;
        }

        if (source.startsWith("${") && source.endsWith("}") && !source.contains("$[")) {
            mod = Ref.get(source);
            type = Type.REF;
            return;
        }

        int dol = source.indexOf("$", -1);

        if (source.toCharArray()[dol + 1] == '{') {
            type = Type.FMT_A;
        }

        if (source.toCharArray()[dol + 1] == '[') {
            type = Type.FMT_B;
        }

        if (type == Type.FMT_A) {
            //扫描里面的$[]块放入args中

            Map<Integer, Integer> points = new LinkedHashMap<>();

            char[] chr = source.toCharArray();
            int lIndex = -1;
            int deep = 0; //深度
            for (int i = 0; i < chr.length; i++) {
                //遇到Ref头符号则深度+1
                if (chr[i] == '$' && chr[i + 1] == '[' && deep == 0) {
                    deep++;
                    lIndex = i;
                }

                //遇到Ref终止符号则深度-1
                if (chr[i] == ']') {
                    deep--;
                    //若深度恰好为0证明一个Ref字符串已解析完毕
                    if (deep == 0) {
                        points.put(lIndex, i + 1);
                    }
                }
            }

            if (deep!=0) {
                throw new IllegalArgumentException("参数解析失败!原因:发现未闭合的计算块");
            }

            this.template = source;
            //替换
            points.entrySet().stream().map((e) -> source.substring(e.getKey(), e.getValue())).peek((v) -> {
                args.add(new ComputeText(v));
            }).forEach((v) -> {
                template = template.replace(v, "%s");
            });
        }

        if (type == Type.FMT_B) {
            //扫描里面的${}块放入args中

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
                throw new IllegalArgumentException("参数解析失败!原因:发现未闭合的取值表达式");
            }

            this.template = source;
            //替换
            points.entrySet().stream().map((e) -> source.substring(e.getKey(), e.getValue())).peek((v) -> {
                args.add(new ComputeText(v));
            }).forEach((v) -> {
                template = template.replace(v, "%s");
            });
        }
        if (args.isEmpty()) {
            //FMT_A在arg为0时会退化为FormatRef
            if (type == Type.FMT_A) {
                type = Type.REF;
                mod = new FormatRef(source);
            }

            //FMT_B在arg为0时代表着abc$[]这样的纯计算式
            if (type == Type.FMT_B) {
                Map<Integer, Integer> points = new LinkedHashMap<>();

                char[] chr = source.toCharArray();
                int lIndex = -1;
                int deep = 0; //深度
                for (int i = 0; i < chr.length; i++) {
                    //遇到Ref头符号则深度+1
                    if (chr[i] == '$' && chr[i + 1] == '[' && deep == 0) {
                        deep++;
                        lIndex = i;
                    }

                    //遇到Ref终止符号则深度-1
                    if (chr[i] == ']') {
                        deep--;
                        //若深度恰好为0证明一个Ref字符串已解析完毕
                        if (deep == 0) {
                            points.put(lIndex, i + 1);
                        }
                    }
                }

                this.template = source;
                //替换
                points.entrySet().stream().map((e) -> source.substring(e.getKey(), e.getValue())).peek((v) -> {
                    args.add(new ComputeText(v));
                }).forEach((v) -> {
                    template = template.replace(v, "%s");
                });
            }
        }
    }

    public Type getType() {
        return type;
    }

    public String getSource() {
        return source;
    }

    @Override
    public Object eval(Map<String, Object> data, Map<String, Object> root) {
        if (type == null) {
            return source;
        }
        switch (type) {
            case REF -> {
                return mod.eval(data, root);
            }
            case MATH -> {
                String result;
                try {
                    result = BigDecimal.valueOf(new ExpressionBuilder(source.substring(2, source.length() - 1)).build().evaluate()).toPlainString();
                } catch (NumberFormatException e) {
                    result = "NaN";
                }
                if (result.endsWith(".0")) { //小数整数化
                    result = result.substring(0, result.length() - 2);
                }
                return result;
            }

            case JSON -> {
                return JSON.parse(source);
            }
            case FMT_A,FMT_B -> {
                Object[] str = new String[args.size()];

                for (int i = 0; i < args.size(); i++) {
                    str[i] = args.get(i).eval(root).toString();
                }
                return new ComputeText(String.format(template,str)).eval(root);
            }
        }
        return source;
    }

    @Override
    public void insert(Map<String, Object> data, Map<String, Object> root, Object value) {
        if (getType() != Type.REF) {
            throw new UnsupportedOperationException();
        }
        mod.insert(data, root, value);
    }


    public enum Type {
        MATH,
        JSON,
        REF,

        FMT_A,
        FMT_B,
    }

    @Override
    public String toString() {
        return source;
    }
}
