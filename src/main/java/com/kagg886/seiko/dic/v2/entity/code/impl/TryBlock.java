package com.kagg886.seiko.dic.v2.entity.code.impl;

import com.kagg886.seiko.dic.v2.session.BasicRuntime;

/**
 * 词库里的try-catch
 *
 * @author kagg886
 * @date 2023/8/18 18:00
 **/
public class TryBlock extends ConditionalExpression {
    private String exceptionVarName = "error";
    public TryBlock(int line, String code) {
        super(line, code);
    }

    public String getExceptionVarName() {
        return exceptionVarName;
    }

    public void setExceptionVarName(String exceptionVarName) {
        this.exceptionVarName = exceptionVarName;
    }

    @Override
    public <T> boolean calc(BasicRuntime<T, ?, ?> runtime) {
        try {
            runtime.invoke(getSuccess(),false);
        } catch (Throwable e) {
            runtime.getRuntimeObject().put(exceptionVarName,e.getMessage() == null ? e.getClass().getName() : e.getMessage());
            runtime.invoke(getFailed(),false);
        }
        return true;
    }
}
