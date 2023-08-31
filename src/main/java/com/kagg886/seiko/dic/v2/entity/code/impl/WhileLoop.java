package com.kagg886.seiko.dic.v2.entity.code.impl;

import com.kagg886.seiko.dic.v2.entity.code.DictionaryCode;
import com.kagg886.seiko.dic.v2.session.BasicRuntime;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kagg886
 * @date 2023/6/3 13:51
 **/
public class WhileLoop extends Expression.If {
    private List<DictionaryCode> loop = new ArrayList<>();

    @Override
    public <T> boolean calc(BasicRuntime<T,?,?> runtime) {
        while (super.calc(runtime)) {
            runtime.invoke(loop,false);
        }
        return true;
    }

    public WhileLoop(int line, String code) {
        super(line, code);
    }

    public List<DictionaryCode> getLoop() {
        return loop;
    }

    public void setLoop(List<DictionaryCode> loop) {
        this.loop = loop;
    }
}
