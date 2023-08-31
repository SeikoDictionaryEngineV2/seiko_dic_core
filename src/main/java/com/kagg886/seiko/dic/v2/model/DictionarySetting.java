package com.kagg886.seiko.dic.v2.model;

/**
 * 词库设置
 *
 * @author kagg886
 * @date 2023/8/31 22:14
 **/
public class DictionarySetting {

    public static final DictionarySetting DEFAULT = new DictionarySetting() {{
        setEnabled(true);
    }};

    private boolean enabled;

    public DictionarySetting() {

    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
