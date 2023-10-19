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

        //设置mock\dic为伪代码文件的路径。伪代码的扫码等均在此进行。
        environment.setDicRoot(base.resolve("dic").toFile());
        //设置mock\data为伪代码文件产生的文件默认仓储的路径。
        environment.setDicData(base.resolve("data").toAbsolutePath());

        //设置dic的启停配置文件。此文件用于管理词库工程启用停止状态
        environment.setDicConfigPoint(base.resolve("config.json").toFile().getAbsolutePath());

        //注册词库方法,K为方法的根标签，V为对应方法的class对象
        environment.getGlobalFunctionRegister().put("测试", Log.class);
        environment.getGlobalFunctionRegister().put("抛出", Throw.class);


        //注册事件筛选器，K为筛选器名称，V为事件的class数组。之所以如此设置是因为一个事件可能会匹配多个class文件
        environment.getEventDomain().put("控制台", new Class[]{String.class});

        //手动调用刷新函数，若报错则会输出错误文件
        for (DICParseResult result : environment.getDicList().refresh()) {
            if (!result.success) {
                result.err.printStackTrace();
            }
        }
        for (DictionaryProject dic : environment.getDicList()) {

            //构造词库Runtime。这个Runtime应该是用完即丢弃的
            LogRuntime runtime = new LogRuntime(dic, "");

            //传入指令，进行词库的运行
            runtime.invoke("qwq1");
        }
    }


    public static class Log extends Function {

        public Log(int line, String code) {
            super(line, code);
        }

        //返回值会被写入到缓冲区中，如不想写入可返回null。
        @Override
        public Object run(BasicRuntime<?, ?, ?> runtime, List<Object> args) {
            return "测试成功";
        }
    }

    public static class Throw extends Function {

        public Throw(int line, String code) {
            super(line, code);
        }

        //若在方法执行过程中抛错且未被捕获，Seiko词库引擎会给出详细的报错信息
        @Override
        public Object run(BasicRuntime<?, ?, ?> runtime, List<Object> args) {
            throw new RuntimeException();
        }
    }

    //继承BasicRuntime，三个泛型分别为：事件class，联系人class，消息缓冲区class
    public static class LogRuntime extends BasicRuntime<String, String, StringBuilder> {

        public LogRuntime(DictionaryProject file, String s) {
            super(file, s);
        }

        //在初始化时调用，此方法负责从事件中抽出联系人。
        @Override
        protected String initContact(String EVENT) {
            return EVENT;
        }

        //初始化消息缓冲区
        @Override
        protected StringBuilder initMessageCache() {
            return new StringBuilder();
        }

        //清空缓冲区前会调用这个方法，在此之前请检查缓冲区是否为null
        @Override
        protected void clearMessage0(StringBuilder stringBuilder) {
            System.out.println(stringBuilder.toString());
        }

        //向缓冲区添加格式化的字符串。
        //若要向缓冲区添加别的元素请使用自定义方法。
        @Override
        protected void appendMessage(String code) {
            getMessageCache().append(code);
        }
    }
}