package com.kagg886.seiko.dic.v2.entity.code.impl;

import com.kagg886.seiko.dic.v2.entity.code.DictionaryCode;
import com.kagg886.seiko.dic.v2.session.BasicRuntime;
import com.kagg886.seiko.dic.v2.util.calc.ComputeText;

/**
 * @projectName: Seiko
 * @package: com.kagg886.seiko.dic.entity.impl
 * @className: Command
 * @author: kagg886
 * @description: 代表一个纯文本
 * @date: 2023/1/16 17:25
 * @version: 1.0
 */
public class PlainText extends DictionaryCode {

    private final ComputeText text;

    public PlainText(int line, String code) {
        super(line, code);
        text = new ComputeText(code);
    }

    public String render(BasicRuntime<?,?,?> runtime) {
        return text.get(runtime.getRuntimeObject()).toString();
    }
}
