package io.github.seikodictionaryenginev2.base.entity.code.impl;

import io.github.seikodictionaryenginev2.base.entity.code.DictionaryCode;
import io.github.seikodictionaryenginev2.base.session.BasicRuntime;

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
            try {
                runtime.invoke(loop,false);
            } catch (NeedContinue e) {
                continue;
            } catch (NeedBreak e) {
                break;
            }
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

    public static class NeedContinue extends RuntimeException {

    }

    public static class NeedBreak extends RuntimeException {

    }
}
