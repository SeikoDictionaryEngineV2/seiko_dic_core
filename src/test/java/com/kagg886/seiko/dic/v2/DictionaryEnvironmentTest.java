package com.kagg886.seiko.dic.v2;

import com.kagg886.seiko.dic.v2.entity.DictionaryFile;
import com.kagg886.seiko.dic.v2.entity.DictionaryProject;
import com.kagg886.seiko.dic.v2.entity.code.func.Function;
import com.kagg886.seiko.dic.v2.env.DictionaryEnvironment;
import com.kagg886.seiko.dic.v2.model.DICParseResult;
import com.kagg886.seiko.dic.v2.session.BasicRuntime;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

class DictionaryEnvironmentTest {

    public static void main(String[] args) {
        new DictionaryEnvironmentTest().getInstance();
    }

    @Test
    void getInstance() {
        DictionaryEnvironment environment = DictionaryEnvironment.getInstance();

        Path base = new File("mock").toPath();

        if (base.toFile().isDirectory()) {
            base.toFile().mkdirs();
        }

        environment.setDicRoot(base.resolve("dic").toFile());
        environment.setDicData(base.resolve("data").toAbsolutePath());
        environment.setDicConfigPoint(base.resolve("config.json").toFile().getAbsolutePath());

        environment.getGlobalFunctionRegister().put("测试", Log.class.getName());
        environment.getGlobalFunctionRegister().put("抛出", Throw.class.getName());


        environment.getEventDomain().put("控制台", new Class[]{String.class});

        for (DICParseResult result : environment.getDicList().refresh()) {
            if (!result.success) {
                result.err.printStackTrace();
            }
        }
        for (DictionaryProject dic : environment.getDicList()) {
            LogRuntime runtime = new LogRuntime(dic, "");
            runtime.invoke("qwq");
        }
    }


    public static class Log extends Function.UnInterruptedFunction {

        public Log(int line, String code) {
            super(line, code);
        }

        @Override
        protected Object run(BasicRuntime<?, ?, ?> runtime, List<Object> args) {
            return "测试成功";
        }
    }

    public static class Throw extends Function.UnInterruptedFunction {

        public Throw(int line, String code) {
            super(line, code);
        }

        @Override
        protected Object run(BasicRuntime<?, ?, ?> runtime, List<Object> args) {
            throw new RuntimeException();
        }
    }

    public static class LogRuntime extends BasicRuntime<String, String, StringBuilder> {

        public LogRuntime(DictionaryProject file, String s) {
            super(file, s);
        }

        @Override
        protected String initContact(String EVENT) {
            return EVENT;
        }

        @Override
        protected StringBuilder initMessageCache() {
            return new StringBuilder();
        }

        @Override
        protected void clearMessage0(StringBuilder stringBuilder) {
            System.out.println(stringBuilder.toString());
        }

        @Override
        protected void appendMessage(String code) {
            getMessageCache().append(code);
        }
    }
}