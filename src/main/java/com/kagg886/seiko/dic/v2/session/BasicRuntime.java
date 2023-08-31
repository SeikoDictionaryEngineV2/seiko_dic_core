package com.kagg886.seiko.dic.v2.session;

import com.kagg886.seiko.dic.v2.entity.DictionaryProject;
import com.kagg886.seiko.dic.v2.entity.code.DictionaryCode;
import com.kagg886.seiko.dic.v2.entity.code.DictionaryCommandMatcher;
import com.kagg886.seiko.dic.v2.entity.DictionaryFile;
import com.kagg886.seiko.dic.v2.entity.code.func.Function;
import com.kagg886.seiko.dic.v2.entity.code.impl.Expression;
import com.kagg886.seiko.dic.v2.entity.code.impl.FastAssignment;
import com.kagg886.seiko.dic.v2.entity.code.impl.PlainText;
import com.kagg886.seiko.dic.v2.env.DictionaryEnvironment;
import com.kagg886.seiko.dic.v2.exception.DictionaryOnRunningException;
import com.kagg886.seiko.dic.v2.model.DictionarySetting;

import java.util.*;

/**
 * @projectName: Seiko
 * @package: com.kagg886.seiko.dic.session
 * @className: AbsRuntime
 * @author: kagg886
 * @description: 代表一次运行时。插件收到事件后，会匹配符合正则表达式的条目，然后构造运行时进行处理
 * @date: 2023/1/12 21:21
 * @version: 1.0
 */
public abstract class BasicRuntime<Event, Contact, MessageCache> {
    private final Event event; //此次执行伪代码所需要的事件
    private Contact contact; //联系人对象，暴露出来是为了往其他群主动发消息用

    private final DictionaryProject project; //被执行的词库项目
    private final DictionaryFile file; //被执行的伪代码指令集
    private final HashMap<String, Object> context; //此次伪代码执行过程中存取的变量
    private final Stack<String> exceptionStacks; //词库调用栈，每次*执行完一条命令*就会存储一条信息到栈中。


    /*
     * @param file: 需要执行的dicFile
     * @param event: 此次执行伪代码所需要的事件
     * @author kagg886
     * @description 构造函数
     * @date 2023/01/19 19:53
     */
    public BasicRuntime(DictionaryProject project, Event event) {
        this.project = project;
        this.file = project.getIndexFile();
        this.event = event;
        context = new HashMap<>();
        exceptionStacks = new Stack<>();
        contact = initContact(event);
    }

    protected abstract Contact initContact(Event EVENT); //根据event初始化联系人对象

    protected abstract MessageCache initMessageCache(); //初始化消息对象

    protected abstract void clearMessage0(MessageCache cache); //清空缓冲区

    protected abstract void appendMessage(String str); //将str放入messageCache中

    public MessageCache getMessageCache() {
        return ((MessageCache) context.get("缓冲区"));
    }

    //如果要添加内置变量，建议在这里添加
    protected void initObject(String command,Event event) {
        getRuntimeObject().put("缓冲区", initMessageCache());
    } //根据command初始化RuntimeObject

    public Stack<String> getExceptionStacks() {
        return exceptionStacks;
    }

    public DictionaryFile getFile() {
        return file;
    }

    public DictionaryProject getProject() {
        return project;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public void clearMessage() { //清空缓冲区，之所以如此设计是因为不同事件发送消息的方法是不同的
        clearMessage0(getMessageCache());
        context.put("缓冲区", initMessageCache());
    }

    public Event getEvent() {
        return event;
    }

    public HashMap<String, Object> getRuntimeObject() {
        return context;
    }
    /*
     * @param command: 指令
     * @return void
     * @author kagg886
     * @description 这个invoke用于匹配合适的伪代码并执行
     * @date 2023/01/19 19:54
     */
    public void invoke(String command) {
        for (Map.Entry<DictionaryCommandMatcher, List<DictionaryCode>> entry : file.getCommands().entrySet()) {
            DictionaryEnvironment env = DictionaryEnvironment.getInstance();
            DictionarySetting setting = Optional.of(env.getDicConfig().getObject(file.getName(), DictionarySetting.class)).orElse(DictionarySetting.DEFAULT);
            if (!setting.isEnabled()) {
                return;
            }

            DictionaryCommandMatcher matcher = entry.getKey();
            List<DictionaryCode> code = entry.getValue();
            if (!matcher.matchesDomain(this)) { //匹配指令触发的环境和当前环境是否相符
                continue;
            }
            if (matcher.matchesCommand(command)) { //正则匹配
                try {
                    initObject(command,event);
                    invoke(code, true);
                } catch (Exception e) { //异常处理，生成调用栈信息向上抛出
                    String msg = e.getMessage();
                    if (e instanceof DictionaryOnRunningException) {
                        msg = ((DictionaryOnRunningException) e).getMsg();
                    }
                    throw new DictionaryOnRunningException(file, msg, this, exceptionStacks, e);
                    //我也不知道这一坨怎么写的，能正常运行就行
                }
            }
        }
    }

    /*
     * @param code: 伪代码集
     * @return void
     * @author kagg886
     * @description 内部invoke函数。将在这里完成对dic最终的解析
     * @date 2023/01/19 19:55
     */
    public void invoke(List<DictionaryCode> code, boolean sendMessageInEnd) {
        boolean sendSwitch = !(code.get(0) instanceof PlainText); //若第一行为PlainText返回false。为Function返回true

        for (DictionaryCode dic : code) {
            exceptionStacks.push(dic.toString());
            if (dic instanceof Function) {
                if (dic instanceof Function.InterruptedFunction) {
                    if (!sendSwitch) {
                        clearMessage();
                        sendSwitch = true;
                    }
                }
                int popStart = exceptionStacks.size();
                Object it = ((Function) dic).invoke(this);
                if (it != null) {
                    appendMessage(it.toString());
                }
                int popEnd = exceptionStacks.size();
                for (int i = popStart; i < popEnd; i++) {
                    exceptionStacks.pop(); //方法成功执行时会移除调用栈
                }
            }

            if (dic instanceof PlainText) {
                appendMessage(((PlainText) dic).render(this));
                sendSwitch = false;
            }

            if (dic instanceof FastAssignment) {
                ((FastAssignment) dic).addInRuntimeObject(this);
            }

            if (dic instanceof Expression.If) {
                ((Expression.If) dic).calc(this);
            }

            if (dic == code.get(code.size() - 1) && sendMessageInEnd) {
                clearMessage();
            }
        }
        //System.out.println(exceptionStacks.toString());
    }
}
