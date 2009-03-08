/*
 * Alipay.com Inc.
 * Copyright (c) 2004-2008 All Rights Reserved.
 */
package org.nuxeo.common.xmap;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * 用来设置<code>Contribution</code>中在<code>Method</code>上标注<code>Annotation</code>
 * 对应值得的<code>getter</code>
 * 
 * @author xi.hux@alipay.com
 * @version $Id: XMethodSetter.java,v 0.1 2008-4-17 上午10:11:17 xi.hux Exp $
 */
public class XMethodSetter implements XSetter {

    /** method */
    private final Method method;

    /**
     * 构建一个<code>XMethodSetter</code>对象
     * 
     * @param method <code>Mehtod</code>实例
     */
    public XMethodSetter(Method method) {
        this.method = method;
        this.method.setAccessible(true);
    }

    /**
     * @see org.nuxeo.common.xmap.XSetter#getType()
     */
    public Class<?> getType() {
        return method.getParameterTypes()[0];
    }

    /**
     * @see org.nuxeo.common.xmap.XSetter#setValue(java.lang.Object, java.lang.Object)
     */
    public void setValue(Object instance, Object value)
            throws IllegalAccessException, InvocationTargetException {
        method.invoke(instance, value);
    }

}
