package io.github.seikodictionaryenginev2.base.entity.code.impl;

import io.github.seikodictionaryenginev2.base.entity.code.DictionaryCode;
import io.github.seikodictionaryenginev2.base.session.BasicRuntime;
import io.github.seikodictionaryenginev2.base.util.calc.ComputeText;

import java.util.HashMap;

/**
 * @Description {C}->{A}
 * {C}->{A.C}
 * {C}->{A.D(0)}
 * {C}->{A.D(0).114}
 * @Author kagg886
 * @Date 2023/11/6
 */
public class VariableInject extends DictionaryCode {
    private final ComputeText var;
    private final ComputeText ref;

    public VariableInject(int line, String code) {
        super(line, code);
        String[] s = code.split("->", 2);

        var = new ComputeText(s[0]);
        ref = new ComputeText(s[1]);

    }

    public void injectObject(BasicRuntime<?, ?, ?> env) {
        ref.set(env.getRuntimeObject(), var.get(env.getRuntimeObject()));
    }
}
