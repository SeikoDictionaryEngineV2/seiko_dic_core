package io.github.seikodictionaryenginev2.base.entity;

import io.github.seikodictionaryenginev2.base.entity.code.DictionaryCode;
import io.github.seikodictionaryenginev2.base.entity.code.DictionaryCommandMatcher;
import io.github.seikodictionaryenginev2.base.entity.code.func.Function;
import io.github.seikodictionaryenginev2.base.entity.code.func.type.DeprecatedFunction;
import io.github.seikodictionaryenginev2.base.entity.code.impl.*;
import io.github.seikodictionaryenginev2.base.exception.DictionaryOnLoadException;
import io.github.seikodictionaryenginev2.base.exception.DictionaryOnRunningException;
import io.github.seikodictionaryenginev2.base.util.ArrayIterator;
import io.github.seikodictionaryenginev2.base.util.IOUtil;
import io.github.seikodictionaryenginev2.base.util.TextUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @projectName: Seiko
 * @package: com.kagg886.seiko.dic.entity
 * @className: Dictionary
 * @author: kagg886
 * @description: 代表了一个伪代码文件
 * @date: 2023/1/9 19:34
 * @version: 1.0
 */
public class DictionaryFile {
    private final DictionaryProject father;

    //代表dic文件路径
    private final File dicFile;

    //dic注册的指令集
    private final HashMap<DictionaryCommandMatcher, List<DictionaryCode>> commands = new HashMap<>();

    //dic的设置集
    private final HashMap<String, String> settings = new HashMap<>();

    public static final String DIC_PREFIX = "#Seiko词库V2";

    public DictionaryFile(DictionaryProject project, File dicFile) {
        this.father = project;
        this.dicFile = dicFile;
        if (!dicFile.exists() || dicFile.isDirectory()) {
            throw new DictionaryOnLoadException(dicFile.getName() + "不存在");
        }
    }

    // 清除变量
    private void clear() {
        commands.clear();
        settings.clear();
    }

    public String getSetting(String str) {
        return settings.get(str);
    }

    //开始解析
    public void parseDICCodeFile() throws IOException {
        // 先clear
        clear();

        String dicCodes = IOUtil.loadStringFromFile(dicFile.getAbsolutePath()).replace("\r", "");
        if (dicCodes.isEmpty()) {
            throw new DictionaryOnLoadException("[" + dicFile.getName() + "]为空!");
        }
        String[] lines = dicCodes.split("\n");
        int start = 0;
        for (int i = 0; i < lines.length; i++) {
            if (!TextUtils.isEmpty(lines[i])) {
                start = i;
                break;
            }
        }
        ArrayIterator<String> iterator = new ArrayIterator<>(lines);
        iterator.setLen(start);

        boolean behindLineIsEmpty = true;
        String commandRegex = null;
        List<DictionaryCode> dictionaryCodes = new ArrayList<>();
        int commandLine = 0; //指令所在的行号
        boolean initConfig = false, //遇到了'#'开头的内容返回true
                initConfigSuccess = false; //在遇到'#'后，若遇到了空行返回true

        while (iterator.hasNext()) {
            String comm = iterator.next();
            if (comm.startsWith("//")) { //注释判空处理
                continue; //注释直接跳过
            }

            //-------------------------解析配置文件-------------------------
            if (!initConfig || !initConfigSuccess) { //跳过解析#的条件:在遇到#后遇到空行
                if (comm.startsWith("#")) {
                    initConfig = true;
                    if (comm.equals(DIC_PREFIX)) {
                        settings.put("引擎版本", comm);
                        continue;
                    }
                    String[] v = comm.substring(1).split(": ", 2);
                    if (v.length == 1) {
                        throw new DictionaryOnLoadException("设置格式不正确!正确的格式为: #键:[空格]值");
                    }
                    settings.put(v[0], v[1]);
                    continue;
                }
                if (TextUtils.isEmpty(comm)) {
                    initConfigSuccess = true;
                    //在此处判断这是不是能正确解析SeikoDIC，先从编码开始
                    if (!settings.containsKey("引擎版本")) {
                        throw new DictionaryOnLoadException("未检测到必要的标识:" + DIC_PREFIX + "。如果你的编辑器里有'" + DIC_PREFIX + "'，那可能说明词库的编码出了问题，请将此词库保存为默认编码以解决本错误。\n" +
                                "出错的伪代码文件:" + this.dicFile.getAbsolutePath() + "\n" +
                                "当前系统默认编码:" + Charset.defaultCharset().displayName());
                    }
                    continue;
                }
                throw new DictionaryOnLoadException("请在伪代码文件开头通过#注册必要设置!\n出错的伪代码文件:" + this.dicFile.getAbsolutePath());
            }

            if (behindLineIsEmpty) {
                /*
                 判断此行的上一行是否为空。
                 若为空则判断此行是否为空，
                 不为空证明此行是指令开始解析指令。
                 */
                if (TextUtils.isEmpty(comm)) {
                    continue;
                }
                commandRegex = comm;
                behindLineIsEmpty = false;
                commandLine = iterator.getLen();
                continue;
            }
            if (TextUtils.isEmpty(comm)) {
                if (dictionaryCodes.isEmpty()) {
                    //排除只有指令没有伪代码实现的情况
                    throw new DictionaryOnLoadException("指令无伪代码实现:" + commandRegex + "(" + dicFile.getName() + ":" + (iterator.getLen() - 1) + ")");
                }
                /*
                    证明这一行指令领导的伪代码已经解析完了，
                    下面的代码用于装载解析完毕的伪代码示例
                 */
                commands.put(new DictionaryCommandMatcher(commandRegex, commandLine, dicFile), dictionaryCodes);
                dictionaryCodes = new ArrayList<>();
                behindLineIsEmpty = true;
                continue;
            }
            /*
                对每一行伪代码进行解析。
                按照[函数->特殊控制字符->纯文本]解析
            */
            iterator.setLen(iterator.getLen() - 1);
            dictionaryCodes = getAllElement(iterator, 0);
            commands.put(new DictionaryCommandMatcher(commandRegex, commandLine, dicFile), dictionaryCodes);
            behindLineIsEmpty = true;
        }
    }

    //获取一整条命令，直到遇到空行
    private List<DictionaryCode> getAllElement(ArrayIterator<String> iterator, int deep) {
        List<DictionaryCode> dictionaryCodes = new ArrayList<>();
        while (iterator.hasNext()) {
            String comm = iterator.next();

            String prefix = TextUtils.repeat(" ", deep);
            if (comm.startsWith(prefix)) { //符合深度，开始填充
                comm = comm.replaceFirst(prefix, ""); //解空格,使用first防止多余的空格被替换

                if (comm.startsWith("如果:")) {
//                    System.out.println(iterator.getLen() + ":" + comm);
//                    getAllElement(iterator, deep + 1);
                    ConditionalExpression expression = new ConditionalExpression(iterator.getLen(), comm);
                    expression.setSuccess(getAllElement(iterator, deep + 1));
                    comm = iterator.now();
                    if (comm.startsWith(prefix + "如果尾")) {
                        iterator.next(); //如果尾需要排除
                        expression.setFailed(getAllElement(iterator, deep + 1));
                    }
                    dictionaryCodes.add(expression);
                } else if (comm.startsWith("试错:")) {
//                    System.out.println(iterator.getLen() + ":" + comm);
//                    getAllElement(iterator, deep + 1);
                    TryBlock tryBlock = new TryBlock(iterator.getLen(), comm);
                    tryBlock.setSuccess(getAllElement(iterator, deep + 1));
                    comm = iterator.now();
                    if (comm.startsWith(prefix + "捕获")) {
//                        iterator.next(); //捕获需要扔掉
                        if (!comm.replace(prefix + "捕获", "").replace(":","").isEmpty()) {
                            tryBlock.setExceptionVarName(iterator.previewNext().replace(prefix + "捕获:", ""));
                        }
                        iterator.next();
                        tryBlock.setFailed(getAllElement(iterator, deep + 1));
                    }
                    dictionaryCodes.add(tryBlock);
                } else if (comm.startsWith("循环:")) {
//                    System.out.println(iterator.getLen() + ":" + comm);
//                    getAllElement(iterator, deep + 1);
                    WhileLoop loop = new WhileLoop(iterator.getLen(), comm);
                    loop.setLoop(getAllElement(iterator, deep + 1));
                    dictionaryCodes.add(loop);
                } else if (comm.equals("跳出")) {
//                    System.out.println(iterator.getLen() + ":" + comm);
                    dictionaryCodes.add(new Expression.Break(iterator.getLen(), comm));
                } else if (comm.equals("跳过")) {
//                    System.out.println(iterator.getLen() + ":" + comm);
                    dictionaryCodes.add(new Expression.Continue(iterator.getLen(), comm));
                } else {
//                    System.out.println(iterator.getLen() + ":" + comm);
                    if (TextUtils.isEmpty(comm)) {
                        return dictionaryCodes;
                    }
                    //解析其他地方的表达式
                    if (comm.startsWith("$") && comm.endsWith("$")) { //真的会有人最后一行跟换行符(
                        try {
                            Function func = Function.parse(comm, iterator.getLen(), father);
                            if (func instanceof DeprecatedFunction) {
                                throw new DictionaryOnRunningException("发现过时函数:" + func.getCode() + "\n" + ((DeprecatedFunction) func).getAdvice());
                            }
                            dictionaryCodes.add(func);
                        } catch (Throwable e) {
                            throw new DictionaryOnLoadException("解析伪代码方法时出错!" + "(" + iterator.getLen() + ":" + comm + ")", e);
                        }
                    } else if (comm.contains("<-") && !comm.contains("\\<-")) {
                        dictionaryCodes.add(new FastAssignment(iterator.getLen(), comm));
                    } else {
                        dictionaryCodes.add(new PlainText(iterator.getLen(), comm));
                    }
                }
            } else {
                iterator.setLen(iterator.getLen() - 1);
                return dictionaryCodes;
            }
        }
        return dictionaryCodes;
    }

    public File getFile() {
        return dicFile;
    }

    public HashMap<DictionaryCommandMatcher, List<DictionaryCode>> getCommands() {
        return commands;
    }


    //获取DicFile的名字，注意和DicProject的名字区分开
    public String getName() {
        return getFile().getName();
    }

    public DictionaryProject getFather() {
        return father;
    }
}
