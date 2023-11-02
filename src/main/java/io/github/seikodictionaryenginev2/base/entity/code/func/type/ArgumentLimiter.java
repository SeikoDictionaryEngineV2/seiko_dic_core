package io.github.seikodictionaryenginev2.base.entity.code.func.type;

/**
 * @projectName: Seiko
 * @package: com.kagg886.seiko.dic.entity.impl
 * @className: ArgumentLimiter
 * @author: kagg886
 * @description: 限制了参数的Function，最多只能拥有getArgumentLength()个参数
 * @date: 2023/3/28 12:42
 * @version: 1.0
 */
public interface ArgumentLimiter {
    int getArgumentLength();
}
