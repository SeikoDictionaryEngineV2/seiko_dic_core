package io.github.seikodictionaryenginev2.base.util.express;

import java.util.Map;

public interface SettableRef {
    void set(Map<String, Object> env, Object value);
}
