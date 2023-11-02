package io.github.seikodictionaryenginev2.base.entity.code.impl;

import io.github.seikodictionaryenginev2.base.entity.code.DictionaryCode;
import io.github.seikodictionaryenginev2.base.entity.code.func.Function;
import io.github.seikodictionaryenginev2.base.exception.DictionaryOnRunningException;
import io.github.seikodictionaryenginev2.base.session.BasicRuntime;
import io.github.seikodictionaryenginev2.base.util.calc.ComputeText;

/**
 * @projectName: Seiko
 * @package: com.kagg886.seiko.dic.entity.impl
 * @className: FastAssignment
 * @author: kagg886
 * @description: 代表快速赋值语句
 * @date: 2023/4/27 19:06
 * @version: 1.0
 */
public class FastAssignment extends DictionaryCode {
    private final String varName;

    private final ComputeText text;

    //此处的code类似A<-表达式，注意此处的表达式如果仅为Ref则需要返回引用结果而不是字符串
    public FastAssignment(int line, String code) {
        //STR<-123456
        //NUM<-6
        //OBJ<-{"A","B"}
        //ARR<-[1,2,3]
        //CUS<-%A% %B%

        //FUN<-$读 A.txt key value$
        super(line, code);
        String[] val = code.split("<-", 2);
        varName = val[0];
        text = new ComputeText(val[1]);
    }

    public void addInRuntimeObject(BasicRuntime<?, ?, ?> runtime) {
        String valueRef = text.getSource();
        if (valueRef.startsWith("$") && valueRef.endsWith("$")) { //方法返回值注入模式
            try {
                Function method = Function.parse(valueRef, getLine(), runtime.getFile().getFather());
                Object rtn = method.invoke(runtime);
                if (rtn == null) {
                    throw new DictionaryOnRunningException(String.format("无法取得方法值:%s，因为方法值为空", method.getCode()));
                }
                runtime.getRuntimeObject().put(varName, rtn);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return;
        }

        //赋值模式
        runtime.getRuntimeObject().put(varName, text.get(runtime.getRuntimeObject()));
/*        try {
            runtime.getRuntimeObject().put(varName, JSON.parseObject(valueRef));
            return;
        } catch (Exception ignored) {

        }
        runtime.getRuntimeObject().put(varName, DictionaryUtil.cleanVariableCode(valueRef, runtime));*/
    }
}
