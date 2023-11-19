package com.gdet.testapp.annotation;

/**
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2023-11-19
 * 描述：
 */
public interface IBinder<T> {
    void bind(T target);
}
