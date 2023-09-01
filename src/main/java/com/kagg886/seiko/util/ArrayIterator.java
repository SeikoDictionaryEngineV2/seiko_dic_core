package com.kagg886.seiko.util;

import java.util.Iterator;

/**
 * @projectName: Seiko
 * @package: com.kagg886.seiko.dic.util
 * @className: ArrayIterator
 * @author: kagg886
 * @description: 基于数组的迭代器
 * @date: 2023/1/11 15:55
 * @version: 1.0
 */
public class ArrayIterator<T> implements Iterator<T> {
    private final T[] array;

    private int len;

    public int size() {
        return array.length;
    }

    public ArrayIterator(T[] t) {
        this.array = t;
    }

    @Override
    public boolean hasNext() {
        return len < array.length;
    }

    public T previewNext() {
        return array[len+1];
    }
    @Override
    public T next() {
        return array[len++];
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public T before() {
        return array[len - 1];
    }
}