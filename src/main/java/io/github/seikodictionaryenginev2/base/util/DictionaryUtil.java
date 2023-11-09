package io.github.seikodictionaryenginev2.base.util;

import io.github.seikodictionaryenginev2.base.exception.DictionaryOnRunningException;
import io.github.seikodictionaryenginev2.base.util.calc.ComputeText;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @projectName: Seiko
 * @package: com.kagg886.seiko.dic
 * @className: DictionaryUtil
 * @author: kagg886
 * @description: 伪代码常用操作
 * @date: 2023/1/16 19:06
 * @version: 1.0
 */
public class DictionaryUtil {
    /*
     * @param :
     * @return String
     * @author kagg886
     * @description 将{a.b.c}或{a(0).b}转换成字符串
     * @date 2023/01/13 09:44
     */

    public static String cleanVariableCode(String code, Map<String,Object> runtime) {
        String clone = code.replace("\\n", "\n");
        ComputeText text = new ComputeText(clone);
        return String.valueOf(text.get(runtime));
    }


    /*
     * @param str: 传入的布尔表达式
     * @return boolean
     * @author kagg886
     * @description 评估布尔表达式
     * @date 2023/01/28 21:33
     */
    public static boolean evalBooleanExpression(String str, Map<String,Object> runtime) {
        if (str == null || str.equals("")) {
            throw new NullPointerException("表达式为空");
        }
        str = str.replace(" ", "");

        if (str.equals("true")) {
            return true;
        }

        if (str.equals("false")) {
            return false;
        }

        //使用增强表达式参与if计算若出现数组引用会被误识别
        try {
            if (str.contains(")")) {
                int lIndex = str.lastIndexOf("(");
                int rIndex = str.indexOf(")", lIndex);
                boolean p = evalBooleanExpression(str.substring(lIndex + 1, rIndex), runtime);
                return evalBooleanExpression(str.replace("(" + str.substring(lIndex + 1, rIndex) + ")", Boolean.toString(p)), runtime);
            }
        } catch (Exception e) {

        }

        if (str.contains("||")) {
            int idx = str.indexOf("||");
            return evalBooleanExpression(str.substring(0, idx), runtime) || evalBooleanExpression(str.substring(idx + 2), runtime);
        }

        if (str.contains("&&")) {
            int idx = str.indexOf("&&");
            return evalBooleanExpression(str.substring(0, idx), runtime) && evalBooleanExpression(str.substring(idx + 2), runtime);
        }

        Function<String,Double> varCalc = (deal) -> {
            try {
                return Double.parseDouble(deal);
            } catch (NumberFormatException e) {
                ComputeText text = new ComputeText("[" + deal + "]");
                return Double.parseDouble(text.get(runtime).toString());
            }
        };

        if (str.contains("==")) {
            int idx = str.indexOf("==");
            try {
                return Objects.equals(varCalc.apply(str.substring(0, idx)), varCalc.apply(str.substring(idx + 2)));
            } catch (Exception e) {
                // 代表等式左边或右边是字符串，按照字符串进行匹配
                return DictionaryUtil.cleanVariableCode(str.substring(0, idx), runtime).equals(DictionaryUtil.cleanVariableCode(str.substring(idx + 2), runtime));
            }
        }

        if (str.contains("!=")) {
            int idx = str.indexOf("!=");
            try {
                return !Objects.equals(varCalc.apply(str.substring(0, idx)), varCalc.apply(str.substring(idx + 2)));
            } catch (Exception e) {
                return !DictionaryUtil.cleanVariableCode(str.substring(0, idx), runtime).equals(DictionaryUtil.cleanVariableCode(str.substring(idx + 2), runtime));
            }
        }


        if (str.contains(">=")) {
            int idx = str.indexOf(">=");
            return varCalc.apply(str.substring(0, idx)) >= varCalc.apply(str.substring(idx + 2));
        }
        if (str.contains("<=")) {
            int idx = str.indexOf("<=");
            return varCalc.apply(str.substring(0, idx)) <= varCalc.apply(str.substring(idx + 2));

        }
        if (str.contains(">")) {
            int idx = str.indexOf(">");
            return varCalc.apply(str.substring(0, idx)) > varCalc.apply(str.substring(idx + 1));
        }
        if (str.contains("<")) {
            int idx = str.indexOf("<");
            return varCalc.apply(str.substring(0, idx)) < varCalc.apply(str.substring(idx + 1));

        }
        throw new DictionaryOnRunningException("计算表达式出错!" + str);
    }
}
