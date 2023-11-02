package io.github.seikodictionaryenginev2.base.entity.code.func;

import io.github.seikodictionaryenginev2.base.entity.DictionaryProject;
import io.github.seikodictionaryenginev2.base.entity.code.DictionaryCode;
import io.github.seikodictionaryenginev2.base.entity.code.func.type.ArgumentLimiter;
import io.github.seikodictionaryenginev2.base.session.BasicRuntime;
import io.github.seikodictionaryenginev2.base.util.calc.ComputeText;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @projectName: Seiko
 * @package: com.kagg886.seiko.dic.entity.impl
 * @className: Function
 * @author: kagg886
 * @description: 代表一个函数
 * @date: 2023/1/16 17:25
 * @version: 1.0
 */
public abstract class Function extends DictionaryCode {

    public static final HashMap<String, Class<? extends Function>> globalManager = new HashMap<>();

    private final String argCode; //去除包装后剩下的参数字符串

    public Function(int line, String code) {
        super(line, code);
        int sIndex = code.indexOf(" ");
        argCode = code.substring(sIndex + 1, code.length() - 1);
    }

    public static HashMap<String, Class<? extends Function>> getGlobalManager() {
        return globalManager;
    }

    /*
     * @param dicLine:
     * @param line:
     * @return Function
     * @author kagg886
     * @description 返回一个伪代码函数对象。使用反射实现
     * @date 2023/01/28 21:37
     */
    private static Function parseFunction(String dicLine, int line,HashMap<String,Class<? extends Function>> funcs) throws Throwable { //一定是$xxxx a b c$
        for (Map.Entry<String, Class<? extends Function>> s : funcs.entrySet()) {
            int spaceIndex = dicLine.indexOf(" ");
            if (spaceIndex == -1) {
                spaceIndex = dicLine.length() - 1; //无参方法解析
            }
            String command = dicLine.substring(1, spaceIndex);
            if (command.equals(s.getKey())) {
                return s.getValue().getConstructor(int.class, String.class).newInstance(line, dicLine);
            }
        }
        throw new NoSuchFieldException("未找到伪代码方法");
    }

    //根据DictionaryProject解析函数。按照先global后context的原则
    public static Function parse(String dicLine, int line, DictionaryProject project) throws Throwable {
        try {
            return parseFunction(dicLine,line,globalManager);
        } catch (Throwable e) {
            return parseFunction(dicLine, line,project.getImports());
        }
    }

    public Object invoke(BasicRuntime<?, ?, ?> runtime) {
        int limit = 0;
        if (this instanceof ArgumentLimiter) {
            limit = ((ArgumentLimiter) this).getArgumentLength();
        }
        List<Object> args = Arrays.stream(argCode.split(" ", limit))
                .map(ComputeText::new)
                .map((v) -> v.get(runtime.getRuntimeObject()))
                .collect(Collectors.toList());
        return invoke(runtime, args);
    }

    public Object invoke(BasicRuntime<?, ?, ?> runtime, List<Object> args) {
        return run(runtime, args);
    }

    protected abstract Object run(BasicRuntime<?, ?, ?> runtime, List<Object> args);
}
