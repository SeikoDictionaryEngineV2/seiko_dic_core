package io.github.seikodictionaryenginev2.base.util.calc;

import com.alibaba.fastjson2.JSON;
import io.github.seikodictionaryenginev2.base.util.express.Ref;
import io.github.seikodictionaryenginev2.base.util.express.SettableRef;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.math.BigDecimal;
import java.util.*;

/**
 * Ref的增强版，在Ref的基础上允许嵌套方括号算术表达式
 *
 * @author kagg886
 * @date 2023/8/28 20:43
 **/
public class ComputeText implements Ref {
    private final String source;
    private String format;

    private Type type;

    private final List<ComputeText> args = new ArrayList<>();

    public Type getType() {
        return type;
    }

    public ComputeText(String source) {
        this.source = source;
        if (source.startsWith("[") && source.endsWith("]")) {
            type = Type.MATH;
        } else if (source.startsWith("{") && source.endsWith("}")) {
            type = Type.REF;
        } else {
            type = Type.STRING;
        }
        try {
            JSON.parse(source);
            type = Type.JSON;
            return;
        } catch (Exception ignored) {
        }

        if (type != Type.STRING) {
            source = source.substring(1, source.length() - 1);
        }

        char[] chars = source.toCharArray();

        int braceLIndex = 0;
        int braceDepth = 0;
        int squareLIndex = 0;
        int squareDepth = 0;

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            if (c == '{') {
                if (braceDepth == 0) {
                    braceLIndex = i;
                }
                braceDepth++;
            }

            if (c == '}') {
                braceDepth--;
                if (braceDepth == 0 && squareDepth == 0) {
                    result.append("%s");
                    args.add(new ComputeText(source.substring(braceLIndex, i + 1)));
                    continue;
                }
            }

            if (c == '[') {
                if (squareDepth == 0) {
                    squareLIndex = i;
                }
                squareDepth++;
            }
            if (c == ']') {
                squareDepth--;
                if (squareDepth == 0 && braceDepth == 0) {
                    result.append("%s");
                    args.add(new ComputeText(source.substring(squareLIndex, i + 1)));
                    continue;
                }
            }

            //深度全部为0证明此时的c不在表达式里，需要放到result中
            if (braceDepth == 0 && squareDepth == 0) {
                result.append(c);
            }
        }

        this.format = result.toString();
    }

    public void set(Map<String, Object> env, Object value) {
        if (type == Type.REF) {
            ((SettableRef) Ref.getRef("{" + formatToRefString(env) + "}")).set(env, value);
            return;
        }
        throw new UnsupportedOperationException("不允许为非表达式赋值");
    }

    private String formatToRefString(Map<String, Object> env) {
        Object[] formatClone = new Object[this.args.size()];
        for (int i = 0; i < args.size(); i++) {
            formatClone[i] = args.get(i).get(env).toString();
        }
        return String.format(format, formatClone).replace("\\n", "\n");
    }

    //对于SOURCE应该返回SOURCE本身
    //对于STRING和MATH表达式返回String
    //对于REF表达式返回Ref查询结果
    @Override
    public Object get(Map<String, Object> env) {
        if (type == Type.JSON) {
            try {
                return JSON.parse(source);
            } catch (Exception e) {
                throw new IllegalArgumentException(source + "不是一个合法的json!");
            }
        }
        String cw = formatToRefString(env);

        if (type == Type.STRING) {
            return cw;
        }
        if (type == Type.MATH) {
            String result;
            try {
                //对于[1,2,{A}]，此代码会报错。
                result = new BigDecimal(Double.toString(new ExpressionBuilder(cw).build().evaluate())).toPlainString();
            } catch (IllegalArgumentException e) {
                //检查一下是不是JSONArray被误解析。
                ComputeText text = new ComputeText("[" + cw + "]");
                try {
                    return text.get(env);
                } catch (Exception ignored) {
                }

                result = "NaN";
            }
            if (result.endsWith(".0")) { //小数整数化
                result = result.substring(0, result.length() - 2);
            }
            return result;
        }
        try {
            //对于{"A":{X}}此代码会报错。
            return Optional.ofNullable(Ref.getRef("{" + cw + "}").get(env)).orElseThrow();
        } catch (NoSuchElementException e) {
            //有可能是JSONObject误包装
            return JSON.parseObject("{" + cw + "}");
        }
    }

    @Override
    public String toString() {
        if (Ref.DEBUG) {
            final StringBuilder sb = new StringBuilder("ComputeText{");
            sb.append("format='").append(format).append('\'');
            sb.append(", type=").append(type);
            sb.append(", args=").append(args);
            sb.append('}');
            return sb.toString();
        }
        return source;
    }

    public String getSource() {
        return source;
    }

    //代表最终对Get处理结果的差异
    public enum Type {
        JSON, //对于误解析字符串应该返回其本身，例如json
        STRING, //代表CT应该是一个字符串
        MATH, //代表CT应该是一个数学表达式
        REF //代表CT应该是一个Ref表达式
    }
}
